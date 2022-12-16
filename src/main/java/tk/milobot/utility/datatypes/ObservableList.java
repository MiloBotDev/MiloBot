package tk.milobot.utility.datatypes;

import tk.milobot.utility.Observer;

import java.util.ArrayList;
import java.util.List;

public class ObservableList<T> extends ArrayList<T> {

    private final List<Observer> observers;

    public ObservableList() {
        observers = new ArrayList<>();
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public boolean add(T t) {
        boolean result = super.add(t);
        notifyObservers();
        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = super.remove(o);
        notifyObservers();
        return result;
    }

    private void notifyObservers() {
        for (Observer observer : this.observers) {
            observer.update();
        }
    }
}
