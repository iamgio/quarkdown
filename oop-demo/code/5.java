class A {
    int i;
    int j = 2;
    Object o;
}

A obj = new A();
int a = obj.i; // a varrà 0
int b = obj.j; // b varrà 2
obj.i = 10     // modifico lo stato di obj
int d = obj.i; // d varrà 10
obj.o = new Object(); // riassegno obj.o
A obj2 = new A();
obj.i = obj2.i; // quanto varrà ora obj.i?