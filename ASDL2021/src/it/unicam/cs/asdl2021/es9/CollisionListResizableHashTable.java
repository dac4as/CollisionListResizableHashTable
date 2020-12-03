/**
 * 
 */
package it.unicam.cs.asdl2021.es9;

import java.util.*;



/**
 * Realizza un insieme tramite una tabella hash con indirizzamento primario (la
 * funzione di hash primario deve essere passata come parametro nel costruttore
 * e deve implementare l'interface PrimaryHashFunction) e liste di collisione.
 * 
 * La tabella, poiché implementa l'interfaccia Set<E> non accetta elementi
 * duplicati (individuati tramite il metodo equals() che si assume sia
 * opportunamente ridefinito nella classe E) e non accetta elementi null.
 * 
 * La tabella ha una dimensione iniziale di default (16) e un fattore di
 * caricamento di defaut (0.75). Quando il fattore di bilanciamento effettivo
 * eccede quello di default la tabella viene raddoppiata e viene fatto un
 * riposizionamento di tutti gli elementi.
 * 
 * @author Template: Luca Tesei, Implementazione: collettiva
 *
 */
public class CollisionListResizableHashTable<E> implements Set<E> {

    /*
     * La capacità iniziale. E' una potenza di due e quindi la capacità sarà
     * sempre una potenza di due, in quanto ogni resize raddoppia la tabella.
     */
    private static final int INITIAL_CAPACITY = 16;

    /*
     * Fattore di bilanciamento di default. Tipico valore.
     */
    private static final double LOAD_FACTOR = 0.75;

    /*
     * Numero di elementi effettivamente presenti nella hash table in questo
     * momento. ATTENZIONE: questo valore è diverso dalla capacity, che è la
     * lunghezza attuale dell'array di Object che rappresenta la tabella.
     */
    private int size;

    /*
     * L'idea è che l'elemento in posizione i della tabella hash è un bucket che
     * contiene null oppure il puntatore al primo nodo di una lista concatenata
     * di elementi. Si può riprendere e adattare il proprio codice della
     * Esercitazione 6 che realizzava una lista concatenata di elementi
     * generici. La classe interna Node<E> è ripresa proprio da lì.
     * 
     * ATTENZIONE: la tabella hash vera e propria può essere solo un generico
     * array di Object e non di Node<E> per una impossibilità del compilatore di
     * accettare di creare array a runtime con un tipo generics. Ciò infatti
     * comporterebbe dei problemi nel sistema di check dei tipi Java che, a
     * run-time, potrebbe eseguire degli assegnamenti in violazione del tipo
     * effettivo della variabile. Quindi usiamo un array di Object che
     * riempiremo sempre con null o con puntatori a oggetti di tipo Node<E>.
     * 
     * Per inserire un elemento nella tabella possiamo usare il polimorfismo di
     * Object:
     * 
     * this.table[i] = new Node<E>(item, next);
     * 
     * ma quando dobbiamo prendere un elemento dalla tabella saremo costretti a
     * fare un cast esplicito:
     * 
     * Node<E> myNode = (Node<E>) this.table[i];
     * 
     * Ci sarà dato un warning di cast non controllato, ma possiamo eliminarlo
     * con un tag @SuppressWarning,
     */
    private Object[] table;

    /*
     * Funzion di hash primaria usata da questa hash table. Va inizializzata nel
     * costruttore all'atto di creazione dell'oggetto.
     */
    private final PrimaryHashFunction phf;

    /*
     * Contatore del numero di modifiche. Serve per rendere l'iterator
     * fail-fast.
     */
    private int modCount;

    // I due metodi seguenti sono di comodo per gestire la capacity e la soglia
    // oltre la quale bisogna fare il resize.

    /* Numero di elementi della tabella corrente */
    private int getCurrentCapacity() {
        return this.table.length;
    };

    /*
     * Valore corrente soglia oltre la quale si deve fare la resize,
     * getCurrentCapacity * LOAD_FACTOR
     */
    private int getCurrentThreshold() {
        return (int) (getCurrentCapacity() * LOAD_FACTOR);
    }

    /**
     * Costruisce una Hash Table con capacità iniziale di default e fattore di
     * caricamento di default.
     */
    public CollisionListResizableHashTable(PrimaryHashFunction phf) {
        this.phf = phf;
        this.table = new Object[INITIAL_CAPACITY];
        this.size = 0;
        this.modCount = 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean contains(Object o) {
            if(o==null) throw new NullPointerException();        /*
         * ATTENZIONE: usare l'hashCode dell'oggetto e la funzione di hash
         * primaria passata all'atto della creazione: il bucket in cui cercare
         * l'oggetto o è la posizione
         * this.phf.hash(o.hashCode(),this.getCurrentCapacity())
         * 
         * In questa posizione, se non vuota, si deve cercare l'elemento o
         * utilizzando il metodo equals() su tutti gli elementi della lista
         * concatenata lì presente
         * 
         */

        int index = this.phf.hash(o.hashCode(),this.getCurrentCapacity());
        if(table[index]!=null)
        {
            Node<E> itr = (Node<E>) table[index];
            do{
                if(itr.item.equals(o))//viene comparato l'item del nodo, non il nodo in sè
                    return true;
                itr=itr.next;// !equals, quindi passo al prossimo nodo
            }while(itr.next!=null);
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Operazione non supportata");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Operazione non supportata");
    }

    @Override
    public boolean add(E e) {
        if (e==null) throw new NullPointerException();
        if(contains(e)) return false;
        /*
         * ATTENZIONE: usare l'hashCode dell'oggetto e la funzione di hash
         * primaria passata all'atto della creazione: il bucket in cui inserire
         * l'oggetto o è la posizione
         * this.phf.hash(o.hashCode(),this.getCurrentCapacity)
         * 
         * In questa posizione, se non vuota, si deve inserire l'elemento o
         * nella lista concatenata lì presente. Se vuota, si crea la lista
         * concatenata e si inserisce l'elemento, che sarà l'unico.
         * 
         */
        // ATTENZIONE, si inserisca prima il nuovo elemento e poi si controlli
        // se bisogna fare resize(), cioè se this.size >
        // this.getCurrentThreshold()

        int index = this.phf.hash(e.hashCode(), this.getCurrentCapacity());//calcolo dell'indice dell'oggetto da inserire
        Node<E> toAdd=new Node<>(e,null);
        if(table[index]==null)//se l'indice dell'array è vuoto, lo aggiungo direttamente come primo elemento di quell'indice
            table[index]=toAdd;
        else//altrimenti trovo l'ultimo nodo della singlelinkedlist di quell'indice di array
        {
            Node<E> itr = (Node<E>) table[index];
            while(itr.next!=null)
            {
                itr=itr.next;
            }
            itr.next=toAdd;//al next di itr viene aggiunto il nuovo nodo aggiunto
        }
        size++;
        modCount++;

        if(this.size>this.getCurrentThreshold()) resize();
        return true;
    }

    /*
     * Raddoppia la tabella corrente e riposiziona tutti gli elementi. Da
     * chiamare quando this.size diventa maggiore di getCurrentThreshold()
     */
    private void resize() {
        Object[] doubleTable=this.table;//variabile di comodo
        clear();//svuoto la table dell'istanza
        table=new Object[doubleTable.length*2];//table è reinzializzato con la sua dimensione precedente ma x2, con tutti gli oggetti salvati ma da inserire da doubleTable a table
        for(Object o:doubleTable)
        {
            this.add((E) o);//tutto ciò è bellissimo E O
        }
    }

    @Override
    public boolean remove(Object o) {
        if(o==null) throw new NullPointerException();
        if(!contains(o) || isEmpty()) return false;
        /*
         * ATTENZIONE: usare l'hashCode dell'oggetto e la funzione di hash
         * primaria passata all'atto della creazione: il bucket in cui cercare
         * l'oggetto o è la posizione
         * this.phf.hash(o.hashCode(),this.getCurrentCapacity)
         * 
         * In questa posizione, se non vuota, si deve cercare l'elemento o
         * utilizzando il metodo equals() su tutti gli elementi della lista
         * concatenata lì presente. Se presente, l'elemento deve essere
         * eliminato dalla lista concatenata
         * 
         */
        // ATTENZIONE: la rimozione, in questa implementazione, **non** comporta
        // mai una resize "al ribasso", cioè un dimezzamento della tabella se si
        // scende sotto il fattore di bilanciamento desiderato.
        int index = this.phf.hash(o.hashCode(),this.getCurrentCapacity());
        Node<E> node = (Node<E>) table[index];
        Node<E> nodeNext;
        if(node.item.equals(o))
            node=null;
        else {
            while (node.next != null) {
                nodeNext = node.next;
                if (node.item.equals(o)) {
                    node.next = nodeNext.next;
                    node = null;
                    break;
                }
                node = nodeNext;
            }
        }
        size--;
        modCount++;
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        // utilizzare un iteratore della collection e chiamare il metodo
        // contains
        if(c==null) throw new NullPointerException();

        for (Object o : c) {
            if (!contains(c))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        // utilizzare un iteratore della collection e chiamare il metodo add
        if(c==null) throw new NullPointerException();

        for (Object o : c) {
            if (!add((E) o))
            {
                return false;
            }
        }
        return true;

    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Operazione non supportata");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if(c==null) throw new NullPointerException();
        // utilizzare un iteratore della collection e chiamare il metodo remove
        for (Object o : c) {
            if (!remove((E) o))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clear() {
        // Ritorno alla situazione iniziale
        this.table = new Object[INITIAL_CAPACITY];
        this.size = 0;
        this.modCount = 0;
    }

    /*
     * Classe per i nodi della lista concatenata
     */
    private static class Node<E> {
        private E item;

        private Node<E> next;

        /*
         * Crea un nodo "singolo" equivalente a una lista con un solo elemento.
         */
        Node(E item, Node<E> next) {
            this.item = item;
            this.next = next;
        }
    }

    /*
     * Classe che realizza un iteratore per questa hash table. L'ordine in cui
     * vengono restituiti gli oggetti presenti non è rilevante, ma ogni oggetto
     * presente deve essere restituito dall'iteratore una e una sola volta.
     * L'iteratore deve essere fail-fast, cioè deve lanciare una eccezione
     * IllegalStateException se a una chiamata di next() si "accorge" che la
     * tabella è stata cambiata rispetto a quando l'iteratore è stato creato.
     */
    private class Itr implements Iterator<E> {

        private int index;
        Node<E> curr;
        private final int numeroModificheAtteso;

        private Itr() {
            this.numeroModificheAtteso = modCount;
            index = 0;
            curr = null;
        }

        @Override
        public boolean hasNext() {
            if (curr == null && index == 0){
                for (int i = 0; i < getCurrentCapacity(); i++){
                    if ( table[i] != null){
                        return true;
                    }
                }
                return false;
            } else {
                if(curr.next != null){
                    return true;
                } else {
                    for ( int i = ++index; i < getCurrentCapacity(); i++){
                        if ( table[i] != null){
                            return true;
                        }
                    }
                    return false;
                }
            }
        }

        @Override
        public E next() {
            if (!hasNext()){
                throw new NoSuchElementException();
            }
            if (numeroModificheAtteso != modCount){
                throw new ConcurrentModificationException();
            }
            if (curr == null && index == 0){
                for (int i = 0; i < getCurrentCapacity(); i++){
                    if ( table[i] != null){
                        index = i;
                        curr = (Node<E>) table[i];
                        break;
                    }
                }
            } else {
                if(curr.next != null){
                    curr = curr.next;
                } else {
                    for ( int i = ++index; i < getCurrentCapacity(); i++){
                        if ( table[i] != null){
                            index = i;
                            curr = (Node<E>) table[i];
                            break;
                        }
                    }
                }
            }

            return curr.item;
        }

    }

}
