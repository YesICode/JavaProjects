package generator;

import java.util.Observer;

/**
 * Interface with methods for an implementation of the observer pattern.
 */
public interface IObservable {

    /**
     *
     * @param o Add this Observer to the list of registered observers.
     */
    void addObserver(Observer o);

    /**
     *
     * @param o Delete this Observer from list of registered observers
     */
    void deleteObserver(Observer o);
}
