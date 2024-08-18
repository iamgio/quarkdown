class UsePoint3D {
    public static void main(String[] args) {
        Point3D p1 = new Point3D();   // creo un punto p1
        p1.build(10.0, 20.0, 30.0);   // ne imposto i valori
        Point3D p2 = new Point3D();   // creo un punto p2
        p2.build(10.0, 20.0, 30.0);   // ne imposto i valori
        System.out.println("Squared modulus of p1: " + p1.getSquaredModulus());
        System.out.println("is p1 equal to p2? " + p1.isEqual(p2));
    }
}