class Point3D {
    double x; // Nota, l'ordine dei campi è irrilevante
    double y;
    double z;
}

Point3D a = new Point3D(); // Creo due punti, di nome a e b
Point3D b = new Point3D();
a.x = 10.0; // Imposto lo stato di a
a.y = 20.0;
a.z = 30.0;
b.x = a.x * 2  // Imposto lo stato di b
b.y = a.y * 2; // ...a partire da quello di a
b.z = a.z * 2;
int mod2a = a.x * a.x + a.y * a.y + a.z * a.z;
int mod2b = b.x * b.x + b.y * b.y + b.z * b.z;
boolean aGreater = (mod2a > mod2b); // false