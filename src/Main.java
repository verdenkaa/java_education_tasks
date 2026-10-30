public class Main {

    private static void printHeader(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    private static void printAccount(BankAccount acc) {
        System.out.println("  " + acc);
    }

    public static void main(String[] args) {

        printHeader("1. Создание аккаунтов");

        BankAccount rub1 = new BankAccount("1234567890", Currency.RUB, 500_00);
        BankAccount rub2 = new BankAccount("0987654321", Currency.RUB, 50_00);
        BankAccount usd1 = new BankAccount("1111111111", Currency.USD, 300_00);

        printAccount(rub1);
        printAccount(rub2);
        printAccount(usd1);

        // невалидный паспорт
        try {
            new BankAccount("12345", Currency.RUB, 100_00);
        } catch (IllegalArgumentException e) {
            System.out.println("  Исключение при плохом паспорте: " + e.getMessage());
        }


        printHeader("2. Пополнение");

        rub1.deposit(300_00, Currency.RUB);
        rub2.deposit(50_00,  Currency.RUB);
        usd1.deposit(96_00,  Currency.USD);

        printAccount(rub1);
        printAccount(rub2);
        printAccount(usd1);


        printHeader("3. Перевод RUB -> RUB");
        System.out.println("  rub1 переводит rub2 30.00 ₽");

        long totalRubBefore = rub1.getBalance().getAmountInMinorUnits() + rub2.getBalance().getAmountInMinorUnits();
        rub1.transfer(rub2, 30_00, Currency.RUB);
        long totalRubAfter = rub1.getBalance().getAmountInMinorUnits() + rub2.getBalance().getAmountInMinorUnits();

        printAccount(rub1);
        printAccount(rub2);
        System.out.printf("  Сумма rub1 + rub2 было: %d копеек, стало: %d копеек (сохранено: %b)%n", totalRubBefore, totalRubAfter, totalRubBefore == totalRubAfter);

        printHeader("4a. Перевод RUB -> USD (без потерь)");
        System.out.println("  rub1 переводит usd1 96.00 ₽ (1 $ по курсу)");

        long rub1Before = rub1.getBalance().getAmountInMinorUnits();
        long usd1Before = usd1.getBalance().getAmountInMinorUnits();

        rub1.transfer(usd1, 96_00, Currency.RUB);

        long rub1Spent = rub1Before - rub1.getBalance().getAmountInMinorUnits();
        long usd1Gained = usd1.getBalance().getAmountInMinorUnits() - usd1Before;

        printAccount(rub1);
        printAccount(usd1);
        System.out.printf("  rub1 потерял: %d копеек RUB%n", rub1Spent);
        System.out.printf("  usd1 получил: %d центов USD%n", usd1Gained);


        printHeader("4b. Перевод RUB -> USD (с потерями)");
        System.out.println("  rub1 переводит usd1 10.00 ₽");

        rub1Before = rub1.getBalance().getAmountInMinorUnits();
        usd1Before = usd1.getBalance().getAmountInMinorUnits();

        rub1.transfer(usd1, 10_00, Currency.RUB);

        rub1Spent = rub1Before - rub1.getBalance().getAmountInMinorUnits();
        usd1Gained = usd1.getBalance().getAmountInMinorUnits() - usd1Before;

        long usd1GainedInKopeks = usd1Gained * Currency.USD.getPriceInRublesKopecks() / Currency.RUB.getPriceInRublesKopecks();

        printAccount(rub1);
        printAccount(usd1);
        System.out.printf("  rub1 потерял: %d копеек RUB%n", rub1Spent);
        System.out.printf("  usd1 получил: %d центов USD = %d копеек RUB по курсу%n", usd1Gained, usd1GainedInKopeks);
        System.out.printf("  Потеря на усечении: %d копеек (%.2f ₽)%n", rub1Spent - usd1GainedInKopeks, (rub1Spent - usd1GainedInKopeks) / 100.0);


        printHeader("5. Превышение лимита");
        System.out.println("  rub2 (лимит " + new Balance(rub2.getWithdrawLimitInMinorUnits(), Currency.RUB) + ", баланс " + rub2.getBalance() + ") переводит 60.00 ₽");

        long rub2Before = rub2.getBalance().getAmountInMinorUnits();
        try {
            rub2.transfer(rub1, 60_00, Currency.RUB);
        } catch (IllegalArgumentException e) {
            System.out.println("  Исключение: " + e.getMessage());
        }
        System.out.printf("  Баланс rub2 не изменился: было %d, стало %d (совпадает: %b)%n", rub2Before, rub2.getBalance().getAmountInMinorUnits(), rub2Before == rub2.getBalance().getAmountInMinorUnits());

        printHeader("6. Недостаточно средств");
        System.out.println("  usd1 (баланс " + usd1.getBalance() + ", лимит " + new Balance(usd1.getWithdrawLimitInMinorUnits(), Currency.USD) + ") снимает 200.00 $");
        try {
            usd1.withdraw(200_00, Currency.USD);
        } catch (IllegalArgumentException e) {
            System.out.println("  Исключение: " + e.getMessage());
        }

        printHeader("7. Перевод самому себе");
        try {
            rub1.transfer(rub1, 10_00, Currency.RUB);
        } catch (IllegalArgumentException e) {
            System.out.println("  Исключение: " + e.getMessage());
        }

        printHeader("8. Конвертация Balance");

        Balance usdBalance = new Balance(100_00, Currency.USD);
        System.out.println("  Исходный: " + usdBalance);

        Balance inRub = usdBalance.convert(Currency.RUB);
        System.out.println("  В RUB: " + inRub);

        Balance sameCurrency = usdBalance.convert(Currency.USD);
        System.out.println("  В USD (та же валюта): " + sameCurrency);
        System.out.println("  Тот же объект (convert вернул this): " + (sameCurrency == usdBalance));

        Balance tiny = new Balance(1, Currency.RUB);
        System.out.println("  1 копейка RUB в USD: " + tiny.convert(Currency.USD) + " (усечение до нуля)");

        printHeader("9. Финальное состояние");
        printAccount(rub1);
        printAccount(rub2);
        printAccount(usd1);
    }
}