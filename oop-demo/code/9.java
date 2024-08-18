class A {
    int i;

    void print() {
        System.out.println("I'm an object of class A");
        System.out.println("My field value is: " + this.i);
    }
}

...

A obj = new A();
obj.i = 12;
obj.print();