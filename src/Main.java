public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создаем две обычные задачи
        Task task1 = new Task(0, "Купить продукты", "Список продуктов для семьи", Status.NEW);
        Task task2 = new Task(0, "Заплатить счета", "Оплатить коммунальные услуги", Status.IN_PROGRESS);

        task1 = manager.createTask(task1);
        task2 = manager.createTask(task2);

        // Создаем эпик с двумя подзадачами
        Epic epic1 = new Epic(0, "Организация праздника", "Подготовка к семейному празднику", Status.NEW);
        epic1 = (Epic) manager.createEpic(epic1);

        Subtask subtask1 = new Subtask(0, "Заказать кейтеринг", "Выбрать меню и заказать", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(0, "Пригласить гостей", "Составить список и отправить приглашения", Status.NEW, epic1.getId());

        subtask1 = manager.createSubtask(subtask1);
        subtask2 = manager.createSubtask(subtask2);

        // Создаем эпик с одной подзадачей
        Epic epic2 = new Epic(0, "Покупка квартиры", "План покупка недвижимости", Status.NEW);
        epic2 = (Epic) manager.createEpic(epic2);

        Subtask subtask3 = new Subtask(0, "Выбрать район", "Изучить районы для покупки", Status.NEW, epic2.getId());
        subtask3 = manager.createSubtask(subtask3);

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
