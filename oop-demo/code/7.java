class A {
    int i;

    void add(int a) { // input "int a"
        i = i + a;
    }

    int getValue() { // intestazione funzione
        return i;  // corpo funzione
    }
}

A obj = new A();
int v = obj.i;           // vale 0
obj.add(10);             // modifico obj
obj.add(20);             // modifico obj
int v2 = obj.i;          // vale 30
int v3 = obj.getValue(); // vale 30

class A {
    int i;

    void add(int a) {
        this.i = this.i + a; // this.i: il "mio" campo i
    }

    int getValue() {
        return this.i;
    }

    int get() { // Un alias per getValue
        return this.getValue();
    }
}