Cетевое хранилище

Клиент-серверное приложение имитирующее обланчое хранилище данных.
Реализовано ввиде двух частей браузерной и серверной.

Клиент. Стек технологий JavaFX.
Все клиентские файлы находятся в модуле клиент.
Реализация двух окон.
 - Главное окно. Картинка меняется в зависимости от того зарегистрирован пользователь или нет.
При аутентификации открывается папка clientFiles и ее содержимое отображается как содержимое корневой
папки пользователя. На сервере создается(если такой нет) папка с именем пользователя для хранения
имеено его файлов. Пользователь иммет возможность менять папку на клиент, уходить на директорию
выше вплоть до диска C://. На сервере он ограничен своей директорией.
Перед регистрицией виден эркан аутентификации, с возможность переключить его на экран регистрации
нового пользователя.
  - Экран регистриации. Возможность ввести в список пользователей нового и зайти в его учетку.

Сервер. Стек технологий Java(Core, Net), JDBC, Netty, Lombok, SQLite.
Реализована сервер.
Построены два обработчика на основе технологии Netty. Создана директория serverFiles для хранения файлов
на сервере.
 - Первый обработчик обрабатывает команды исключительно регистрации и аутентификации.
При доваблении нового пользователя его логин и пароль остаются в базе данных, под него создается
именная папка.
 - После успешной аутентификации логин пользователя передается в отдельный поток ко второму обработчику,
который работает с остальными командами от сервера.
В отдельном модуле Core есть список команд, которые поддерживаются и на сервере и на клиенте, которые
позволяют добавлять, удалять файлы и менять директорию как на сервере, так и на клиенте.
 - При включении сервера создаются три кастомных пользователя "asd", "qwe", "zxc". Их пароли совпадают
с логинами.
 - База данных реализована inmemory, через JDBC. После выключения сервера созданные пользователи
будут удалены
 - Сервер запускается на порту 8189.

