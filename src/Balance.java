import java.util.Objects;

public final class Balance {
    
    private final long amountInMinorUnits;
    private final Currency currency;

    public Balance(long amountInMinorUnits, Currency currency) {
        if (currency == null) {
        throw new IllegalArgumentException("Валюта не может быть null");
        }
        if (amountInMinorUnits < 0) {
            throw new IllegalArgumentException("Сумма не может быть отрицательной");
        }

        this.amountInMinorUnits = amountInMinorUnits;
        this.currency = currency;
    }

    public long getAmountInMinorUnits() {
        return amountInMinorUnits;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Balance add(long delta, Currency amountCurrency){
        if (delta < 0) {
            throw new IllegalArgumentException("Нельзя уменьшать количество валюты!");
        }
        if (amountCurrency == null) throw new IllegalArgumentException("Валюта не должна быть null");

        delta = new Balance(delta, amountCurrency).convert(this.currency).getAmountInMinorUnits();

        long new_summ = Math.addExact(amountInMinorUnits, delta);
        return new Balance(new_summ, currency);
    }

    public Balance subtract(long delta, Currency amountCurrency){
        if (delta < 0) {
            throw new IllegalArgumentException("Нельзя увеличивать количество валюты!");
        }
        if (amountCurrency == null) throw new IllegalArgumentException("Валюта не должна быть null");

        long amount = new Balance(delta, amountCurrency).convert(this.currency).getAmountInMinorUnits();

        if (amount > amountInMinorUnits){
            throw new IllegalArgumentException("Траты превышают текущий баланс!");
        }

        long new_summ = Math.subtractExact(amountInMinorUnits, amount);
        return new Balance(new_summ, currency);
    }

    public Balance add(long delta) { return add(delta, this.currency); }

    public Balance subtract(long delta) { return subtract(delta, this.currency); }

    public Balance convert(Currency target) {
        /*Округление вниз (усечение), доли минорной единицы теряются.*/

        if (target == null) throw new IllegalArgumentException("Валюта не должна быть null");
        if (target == this.currency) {
            return this;
        }

        long new_amountInMinorUnits = Math.multiplyExact(amountInMinorUnits, currency.getPriceInRublesKopecks());
        new_amountInMinorUnits = new_amountInMinorUnits / target.getPriceInRublesKopecks();
        return new Balance(new_amountInMinorUnits, target);

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Balance other = (Balance) obj;

        return amountInMinorUnits == other.amountInMinorUnits && currency == other.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amountInMinorUnits, currency);
    }

    @Override
    public String toString() {
        long major = amountInMinorUnits / 100;
        long minor = amountInMinorUnits % 100;

        return String.format("%d.%02d %s", major, minor, currency.getSymbol());
    }
}
