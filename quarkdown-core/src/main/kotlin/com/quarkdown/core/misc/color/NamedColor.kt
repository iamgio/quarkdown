package com.quarkdown.core.misc.color

/**
 * The CSS3 color catalogue, associating a color with its name.
 * Original source: [aarongarciah/css-spec-colors](https://github.com/aarongarciah/css-spec-colors/blob/master/docs/colors.json)
 * @param color color value
 */
@Suppress("unused")
enum class NamedColor(
    val color: Color,
) {
    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=AliceBlue
     */
    ALICEBLUE(240, 248, 255),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=AntiqueWhite
     */
    ANTIQUEWHITE(250, 235, 215),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Aqua
     */
    AQUA(0, 255, 255),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Aquamarine
     */
    AQUAMARINE(127, 255, 212),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Azure
     */
    AZURE(240, 255, 255),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Beige
     */
    BEIGE(245, 245, 220),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Bisque
     */
    BISQUE(255, 228, 196),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Black
     */
    BLACK(0, 0, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=BlanchedAlmond
     */
    BLANCHEDALMOND(255, 235, 205),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Blue
     */
    BLUE(0, 0, 255),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=BlueViolet
     */
    BLUEVIOLET(138, 43, 226),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Brown
     */
    BROWN(165, 42, 42),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Burlywood
     */
    BURLYWOOD(222, 184, 135),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=CadetBlue
     */
    CADETBLUE(95, 158, 160),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Chartreuse
     */
    CHARTREUSE(127, 255, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Chocolate
     */
    CHOCOLATE(210, 105, 30),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Coral
     */
    CORAL(255, 127, 80),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=CornflowerBlue
     */
    CORNFLOWERBLUE(100, 149, 237),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Cornsilk
     */
    CORNSILK(255, 248, 220),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Crimson
     */
    CRIMSON(220, 20, 60),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Cyan
     */
    CYAN(0, 255, 255),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkBlue
     */
    DARKBLUE(0, 0, 139),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkCyan
     */
    DARKCYAN(0, 139, 139),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkGoldenrod
     */
    DARKGOLDENROD(184, 134, 11),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkGray
     */
    DARKGRAY(169, 169, 169),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkGreen
     */
    DARKGREEN(0, 100, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkGrey
     */
    DARKGREY(169, 169, 169),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkKhaki
     */
    DARKKHAKI(189, 183, 107),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkMagenta
     */
    DARKMAGENTA(139, 0, 139),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkOliveGreen
     */
    DARKOLIVEGREEN(85, 107, 47),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkOrange
     */
    DARKORANGE(255, 140, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkOrchid
     */
    DARKORCHID(153, 50, 204),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkRed
     */
    DARKRED(139, 0, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkSalmon
     */
    DARKSALMON(233, 150, 122),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkSeaGreen
     */
    DARKSEAGREEN(143, 188, 143),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkSlateBlue
     */
    DARKSLATEBLUE(72, 61, 139),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkSlateGray
     */
    DARKSLATEGRAY(47, 79, 79),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkSlateGrey
     */
    DARKSLATEGREY(47, 79, 79),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkTurquoise
     */
    DARKTURQUOISE(0, 206, 209),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DarkViolet
     */
    DARKVIOLET(148, 0, 211),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DeepPink
     */
    DEEPPINK(255, 20, 147),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DeepSkyBlue
     */
    DEEPSKYBLUE(0, 191, 255),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DimGray
     */
    DIMGRAY(105, 105, 105),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DimGrey
     */
    DIMGREY(105, 105, 105),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=DodgerBlue
     */
    DODGERBLUE(30, 144, 255),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Firebrick
     */
    FIREBRICK(178, 34, 34),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=FloralWhite
     */
    FLORALWHITE(255, 250, 240),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=ForestGreen
     */
    FORESTGREEN(34, 139, 34),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Fuchsia
     */
    FUCHSIA(255, 0, 255),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Gainsboro
     */
    GAINSBORO(220, 220, 220),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=GhostWhite
     */
    GHOSTWHITE(248, 248, 255),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Gold
     */
    GOLD(255, 215, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Goldenrod
     */
    GOLDENROD(218, 165, 32),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Gray
     */
    GRAY(128, 128, 128),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Green
     */
    GREEN(0, 128, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=GreenYellow
     */
    GREENYELLOW(173, 255, 47),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Grey
     */
    GREY(128, 128, 128),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Honeydew
     */
    HONEYDEW(240, 255, 240),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=HotPink
     */
    HOTPINK(255, 105, 180),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=IndianRed
     */
    INDIANRED(205, 92, 92),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Indigo
     */
    INDIGO(75, 0, 130),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Ivory
     */
    IVORY(255, 255, 240),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Khaki
     */
    KHAKI(240, 230, 140),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Lavender
     */
    LAVENDER(230, 230, 250),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LavenderBlush
     */
    LAVENDERBLUSH(255, 240, 245),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Lawngreen
     */
    LAWNGREEN(124, 252, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LemonChiffon
     */
    LEMONCHIFFON(255, 250, 205),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightBlue
     */
    LIGHTBLUE(173, 216, 230),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightCoral
     */
    LIGHTCORAL(240, 128, 128),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightCyan
     */
    LIGHTCYAN(224, 255, 255),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightGoldenrodYellow
     */
    LIGHTGOLDENRODYELLOW(250, 250, 210),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightGray
     */
    LIGHTGRAY(211, 211, 211),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightGreen
     */
    LIGHTGREEN(144, 238, 144),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightGrey
     */
    LIGHTGREY(211, 211, 211),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightPink
     */
    LIGHTPINK(255, 182, 193),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightSalmon
     */
    LIGHTSALMON(255, 160, 122),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightSeaGreen
     */
    LIGHTSEAGREEN(32, 178, 170),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightSkyBlue
     */
    LIGHTSKYBLUE(135, 206, 250),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightSlateGray
     */
    LIGHTSLATEGRAY(119, 136, 153),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightSlateGrey
     */
    LIGHTSLATEGREY(119, 136, 153),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightSteelBlue
     */
    LIGHTSTEELBLUE(176, 196, 222),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LightYellow
     */
    LIGHTYELLOW(255, 255, 224),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Lime
     */
    LIME(0, 255, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=LimeGreen
     */
    LIMEGREEN(50, 205, 50),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Linen
     */
    LINEN(250, 240, 230),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Magenta
     */
    MAGENTA(255, 0, 255),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Maroon
     */
    MAROON(128, 0, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=MediumAquamarine
     */
    MEDIUMAQUAMARINE(102, 205, 170),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=MediumBlue
     */
    MEDIUMBLUE(0, 0, 205),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=MediumOrchid
     */
    MEDIUMORCHID(186, 85, 211),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=MediumPurple
     */
    MEDIUMPURPLE(147, 112, 219),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=MediumSeaGreen
     */
    MEDIUMSEAGREEN(60, 179, 113),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=MediumSlateBlue
     */
    MEDIUMSLATEBLUE(123, 104, 238),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=MediumSpringGreen
     */
    MEDIUMSPRINGGREEN(0, 250, 154),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=MediumTurquoise
     */
    MEDIUMTURQUOISE(72, 209, 204),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=MediumVioletRed
     */
    MEDIUMVIOLETRED(199, 21, 133),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=MidnightBlue
     */
    MIDNIGHTBLUE(25, 25, 112),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=MintCream
     */
    MINTCREAM(245, 255, 250),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=MistyRose
     */
    MISTYROSE(255, 228, 225),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Moccasin
     */
    MOCCASIN(255, 228, 181),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=NavajoWhite
     */
    NAVAJOWHITE(255, 222, 173),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Navy
     */
    NAVY(0, 0, 128),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=OldLace
     */
    OLDLACE(253, 245, 230),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Olive
     */
    OLIVE(128, 128, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=OliveDrab
     */
    OLIVEDRAB(107, 142, 35),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Orange
     */
    ORANGE(255, 165, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=OrangeRed
     */
    ORANGERED(255, 69, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Orchid
     */
    ORCHID(218, 112, 214),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=PaleGoldenrod
     */
    PALEGOLDENROD(238, 232, 170),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=PaleGreen
     */
    PALEGREEN(152, 251, 152),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=PaleTurquoise
     */
    PALETURQUOISE(175, 238, 238),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=PaleVioletRed
     */
    PALEVIOLETRED(219, 112, 147),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=PapayaWhip
     */
    PAPAYAWHIP(255, 239, 213),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=PeachPuff
     */
    PEACHPUFF(255, 218, 185),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Peru
     */
    PERU(205, 133, 63),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Pink
     */
    PINK(255, 192, 203),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Plum
     */
    PLUM(221, 160, 221),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=PowderBlue
     */
    POWDERBLUE(176, 224, 230),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Purple
     */
    PURPLE(128, 0, 128),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=RebeccaPurple
     */
    REBECCAPURPLE(102, 51, 153),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Red
     */
    RED(255, 0, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=RosyBrown
     */
    ROSYBROWN(188, 143, 143),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=RoyalBlue
     */
    ROYALBLUE(65, 105, 225),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=SaddleBrown
     */
    SADDLEBROWN(139, 69, 19),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Salmon
     */
    SALMON(250, 128, 114),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=SandyBrown
     */
    SANDYBROWN(244, 164, 96),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=SeaGreen
     */
    SEAGREEN(46, 139, 87),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Seashell
     */
    SEASHELL(255, 245, 238),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Sienna
     */
    SIENNA(160, 82, 45),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Silver
     */
    SILVER(192, 192, 192),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=SkyBlue
     */
    SKYBLUE(135, 206, 235),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=SlateBlue
     */
    SLATEBLUE(106, 90, 205),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=SlateGray
     */
    SLATEGRAY(112, 128, 144),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=SlateGrey
     */
    SLATEGREY(112, 128, 144),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Snow
     */
    SNOW(255, 250, 250),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=SpringGreen
     */
    SPRINGGREEN(0, 255, 127),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=SteelBlue
     */
    STEELBLUE(70, 130, 180),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Tan
     */
    TAN(210, 180, 140),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Teal
     */
    TEAL(0, 128, 128),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Thistle
     */
    THISTLE(216, 191, 216),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Tomato
     */
    TOMATO(255, 99, 71),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Turquoise
     */
    TURQUOISE(64, 224, 208),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Violet
     */
    VIOLET(238, 130, 238),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Wheat
     */
    WHEAT(245, 222, 179),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=White
     */
    WHITE(255, 255, 255),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=WhiteSmoke
     */
    WHITESMOKE(245, 245, 245),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=Yellow
     */
    YELLOW(255, 255, 0),

    /**
     * https://www.w3schools.com/colors/color_tryit.asp?color=YellowGreen
     */
    YELLOWGREEN(154, 205, 50),
    ;

    constructor(red: Int, green: Int, blue: Int) : this(Color(red, green, blue))
}
