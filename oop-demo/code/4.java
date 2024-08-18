class A {  // A è il nome della classe
    ...    // qui si riporta il suo contenuto
}

class AnotherClassExample {  // Nota il CamelCase
    ...
}

// codice cliente
A obj1 = new A();  // creo un oggetto di A, con nome obj1
A obj2 = new A();  // creo un altro oggetto di A
AnotherClassExample obj3 = new AnotherClassExample();
A obj4; // variabile obj4 non inizializzata
obj4 = new A(); // ok
obj4 = new AnotherClassExample(); // NO!!! Errore semantico...