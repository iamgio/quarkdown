public final class Wrapper<T> {
    private final T value;

    public Wrapper(T value) {
        this.value = value;
    }

    public final T getValue() {
        return this.value;
    }
}