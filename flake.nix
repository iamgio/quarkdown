{
  description =
    "Quarkdown CLI - A modern Markdown-based typesetting system with superpowers";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};

        # Quarkdown version - using latest stable release
        version = "1.4.0";

        # Build Quarkdown from source
        quarkdown = pkgs.stdenv.mkDerivation rec {
          pname = "quarkdown";
          inherit version;

          src = pkgs.fetchFromGitHub {
            owner = "iamgio";
            repo = "quarkdown";
            rev = "v${version}";
            sha256 = "sha256-9WQ3+VjlZmlDcHwSVOWD/ZGJfRCnXdKF0humGq2d8CU=";
          };

          nativeBuildInputs = with pkgs; [
            gradle_8 # Use specific Gradle 8.x for reproducibility
            jdk17
            makeWrapper
            unzip
          ];

          buildInputs = with pkgs; [ jdk17 ];

          # Gradle needs to download dependencies
          GRADLE_USER_HOME = ".gradle";

          configurePhase = ''
            # Set up Gradle home
            export GRADLE_USER_HOME=$(mktemp -d)

            # Ensure we're using JDK 17 to avoid compatibility issues
            export JAVA_HOME=${pkgs.jdk17}
            export PATH=${pkgs.jdk17}/bin:$PATH
          '';

          buildPhase = ''
            echo "Building Quarkdown distribution with Gradle..."
            # Use Nix-provided gradle instead of ./gradlew for reproducibility
            gradle --no-daemon --console=plain -Dorg.gradle.java.home=${pkgs.jdk17} distZip
          '';

          installPhase = ''
            # Extract the distribution ZIP
            cd quarkdown-cli/build/distributions
            unzip quarkdown-cli.zip

            # Create output directories
            mkdir -p $out/bin
            mkdir -p $out/lib/quarkdown
            mkdir -p $out/share/quarkdown

            # Copy the distribution
            cp -r quarkdown-cli/* $out/lib/quarkdown/

            # Copy library files (.qmd files) if they exist
            if [ -d $out/lib/quarkdown/lib/qmd ]; then
              cp -r $out/lib/quarkdown/lib/qmd $out/share/quarkdown/lib/
            fi

            # Create wrapper for the main executable
            makeWrapper $out/lib/quarkdown/bin/quarkdown-cli $out/bin/quarkdown \
              --set JAVA_HOME "${pkgs.jdk17}" \
              --prefix PATH : "${pkgs.jdk17}/bin"
          '';

          # Fix phases for Gradle builds
          dontStrip = true;
          dontPatchELF = true;

          meta = with pkgs.lib; {
            description =
              "A modern Markdown-based typesetting system with superpowers";
            longDescription = ''
              Quarkdown is a modern Markdown-based typesetting system designed around
              versatility. It seamlessly compiles projects into print-ready books,
              interactive presentations, or web documents. Features include:

              - Extended Markdown syntax with functions, loops, and conditionals
              - Live preview with hot reloading
              - PDF export capabilities
              - Built-in standard library for layouts, math, diagrams, and more
              - Project creation wizard
              - Interactive REPL mode
            '';
            homepage = "https://github.com/iamgio/quarkdown";
            license = licenses.gpl3Only;
            maintainers = with maintainers; [ ];
            platforms = platforms.all;
            mainProgram = "quarkdown";
          };
        };

      in {
        packages = { default = quarkdown; };

        # Development shell for working on Quarkdown
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [ jdk17 gradle_8 git ];

          shellHook = ''
            echo "Quarkdown development environment"
            echo "Java version: $(java -version 2>&1 | head -n1)"
            echo "Gradle version: $(gradle --version | grep Gradle)"
            echo ""
            echo "Available commands:"
            echo "  gradle build          - Build the project"
            echo "  gradle distZip        - Create distribution ZIP"
            echo "  gradle test           - Run tests"
            echo ""
            echo "Set JAVA_HOME to JDK 17:"
            export JAVA_HOME=${pkgs.jdk17}
            export PATH=${pkgs.jdk17}/bin:$PATH
          '';
        };

        # Application for easy running
        apps = {
          default = {
            type = "app";
            program = "${quarkdown}/bin/quarkdown";
          };
        };
      });
}
