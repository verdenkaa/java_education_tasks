public class BankAccount {

    private static long nextId = 1; // уникальный id получается через static nextId

    private final long id;
    private final String owner; // паспорт владельца
    private Balance balance;
    private long withdrawLimitInMinorUnits;


    public BankAccount(String owner, Currency currency, long withdrawLimitInMinorUnits) {

        if (owner == null) {
            throw new IllegalArgumentException("Владелец не может быть null");
        }

        if (!owner.matches("\\d{10}")) {
            throw new IllegalArgumentException("Номер паспорта должен содержать ровно 10 цифр, получено: " + owner);
        }

        if (currency == null) {
            throw new IllegalArgumentException("Валюта не может быть null");
        }

        if (withdrawLimitInMinorUnits < 0) {
            throw new IllegalArgumentException("Лимит не может быть отрицательным, получено: " + withdrawLimitInMinorUnits);
        }

        this.owner = owner;
        this.balance = new Balance(0, currency);
        this.withdrawLimitInMinorUnits = withdrawLimitInMinorUnits;
        this.id = nextId++;
    }

    public long getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public Balance getBalance() {
        return balance;
    }

    public long getWithdrawLimitInMinorUnits() {
        return withdrawLimitInMinorUnits;
    }

    public void setWithdrawLimitInMinorUnits(long newLimit) {
        if (newLimit < 0) {
            throw new IllegalArgumentException("Лимит не может быть отрицательным, получено: " + newLimit);
        }
        this.withdrawLimitInMinorUnits = newLimit;
    }

    public void deposit(long amount, Currency currency) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть положительной, получено: " + amount);
        }
        if (currency == null) {
            throw new IllegalArgumentException("Валюта не может быть null");
        }

        this.balance = this.balance.add(amount, currency);
    }

    public void withdraw(long amount, Currency currency) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма снятия должна быть положительной, получено: " + amount);
        }
        if (currency == null) {
            throw new IllegalArgumentException("Валюта не может быть null");
        }

        long amountInAccountCurrency = new Balance(amount, currency)
                .convert(balance.getCurrency())
                .getAmountInMinorUnits();

        if (amountInAccountCurrency > withdrawLimitInMinorUnits) {
            throw new IllegalArgumentException(String.format(
                    "Превышен лимит на снятие: запрошено %d %s (в валюте счёта), лимит %d %s",
                    amountInAccountCurrency,
                    balance.getCurrency().getCode(),
                    withdrawLimitInMinorUnits,
                    balance.getCurrency().getCode()));
        }

        this.balance = this.balance.subtract(amount, currency);
    }

    public void transfer(BankAccount to, long amount, Currency currency) {
        if (to == null) {
            throw new IllegalArgumentException("Аккаунт получателя не может быть null");
        }

        if (to.id == this.id) {
            throw new IllegalArgumentException("Нельзя переводить на тот же счёт (id=" + this.id + ")");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной, получено: " + amount);
        }
        if (currency == null) {
            throw new IllegalArgumentException("Валюта не может быть null");
        }

        long amountInSenderCurrency = new Balance(amount, currency)
                .convert(balance.getCurrency())
                .getAmountInMinorUnits();

        if (amountInSenderCurrency > withdrawLimitInMinorUnits) {
            throw new IllegalArgumentException(String.format(
                    "Превышен лимит на перевод: запрошено %d %s (в валюте отправителя), лимит %d %s",
                    amountInSenderCurrency,
                    balance.getCurrency().getCode(),
                    withdrawLimitInMinorUnits,
                    balance.getCurrency().getCode()));
        }

        this.balance = this.balance.subtract(amount, currency);
        to.balance   = to.balance.add(amount, currency);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BankAccount other = (BankAccount) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format(
                "BankAccount{id=%d, owner=%s, balance=%s, withdrawLimit=%s}",
                id,
                owner,
                balance,
                new Balance(withdrawLimitInMinorUnits, balance.getCurrency()));
    }
}