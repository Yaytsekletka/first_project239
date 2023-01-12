import org.junit.Test;

/**
 * Класс тестирования
 */
public class UnitTest {

    /**
     * Первый тест
     */
    @Test
    public void firstTest() {
        // сумма изначально равна 0
        int sum = 0;
        // суммируем числа от 0 до 9
        for (int i = 0; i < 10; i++) {
            sum += i;
        }
        // проверяем, равна ли сумма 45
        assert sum == 45;
    }

    /**
     * Второй тест
     */
    @Test
    public void secondTest() {
        // сумма изначально равна 0
        int sum = 0;
        // суммируем числа от 0 до 19
        for (int i = 0; i < 20; i++) {
            sum += i;
        }
        // проверяем, равна ли сумма 190
        assert sum == 190;
    }
}
