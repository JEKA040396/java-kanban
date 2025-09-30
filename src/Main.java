import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import manager.Managers;


public class Main {
    public static void main(String[] args) {

        TaskManager manager = Managers.getDefault();

        // Создаем две обычные задачи
        Task task1 = new Task(0, "Купить продукты", "Список продуктов для семьи", Status.NEW);
        Task task2 = new Task(0, "Заплатить счета", "Оплатить коммунальные услуги", Status.IN_PROGRESS);

        task1 = manager.createTask("Купить продукты", "Список продуктов для семьи", Status.NEW);
        task2 = manager.createTask("Заплатить счета", "Оплатить коммунальные услуги", Status.IN_PROGRESS);

        // Создаем эпик с двумя подзадачами
        Epic epic1 = manager.createEpic("Организация праздника", "Подготовка к семейному празднику", Status.NEW);


        Subtask subtask1 = manager.createSubtask("Заказать кейтеринг", "Выбрать меню и заказать", Status.NEW, epic1.getId());

        Subtask subtask2 = manager.createSubtask("Пригласить гостей", "Составить список и отправить приглашения", Status.NEW, epic1.getId());

        // Создаем эпик с одной подзадачей
        Epic epic2 = manager.createEpic("Покупка квартиры", "План покупка недвижимости", Status.NEW);

        Subtask subtask3 = manager.createSubtask("Выбрать район", "Изучить районы для покупки", Status.NEW, epic2.getId());


        // Выводим списки всех задач
        System.out.println("Все задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nВсе эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("\nВсе подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        // Изменение статусов
        subtask1.setStatus(Status.DONE);
        manager.updateTask(subtask1);

        subtask2.setStatus(Status.DONE);
        manager.updateTask(subtask2);

        // Обновляем статус эпика после изменения подзадач
        epic1.updateStatus();
        manager.updateTask(epic1);

        System.out.println("\nСтатусы после обновления подзадач эпика:");
        System.out.println("Эпик 1: " + epic1);
        System.out.println("Подзадачи эпика 1:");
        for (Subtask sub : manager.getSubtasksOfEpic(epic1.getId())) {
            System.out.println(sub);
        }

        // Удаление задачи и эпика
        manager.removeById(task1.getId());
        manager.removeById(epic2.getId());

        System.out.println("\nПосле удаления одной задачи и одного эпика:");
        System.out.println("Все задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Все эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }
        System.out.println("Все подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }
}
