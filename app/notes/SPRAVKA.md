# Справка по проекту DELI

---

# 1. MainActivity.kt

Главный файл приложения.
Здесь создаётся навигация между экранами и подключаются ViewModel и уведомления.

## Переменные

VIEWMODEL - главный ViewModel, хранит тему и данные профиля пользователя

FRIENDSVIEWMODEL - ViewModel для работы с друзьями, заявками и поиском

CONTEXT - контекст приложения, нужен для DataStore и уведомлений

ISDARKTHEME - текущее состояние темы. true = тёмная, false = светлая

DOLZHNIKI - список должников, хранится в памяти через mutableStateListOf

SOBITIYA - список событий, хранится в памяти через mutableStateListOf

FRIENDS - список друзей, приходит из FriendsViewModel

SENTREQUESTS - список отправленных заявок в друзья

INCOMINGREQUESTS - список входящих заявок в друзья

NAVCONTROLLER - контроллер навигации, управляет переходами между экранами

INNERPADDING - отступы от системных элементов, статус-бар и нижняя навигация

NOTIFICATIONPERMISSIONLAUNCHER - запрашивает разрешение на уведомления при старте

## Навигация

screen_1 - стартовый экран MainScreen

screen_2 - экран входа и регистрации SecondScreen

screen_3 - главный экран приложения ThirdMainScreen

screen_4 - экран создания события DobavitSobitie

screen_5 - экран добавления должника DobavitDolshnika

screen_6 - экран профиля Profile

screen_7 - экран друзей FriendsScreen

## Уведомления

При старте приложения запрашивается разрешение POST_NOTIFICATIONS для Android 13 и выше.
NotificationHelper.createChannel вызывается один раз при старте и создаёт канал уведомлений.
При добавлении должника или события вызывается NotificationScheduler.scheduleDeadlineNotifications и планируются два уведомления: за неделю и за день до дедлайна.
При удалении или оплате вызывается NotificationScheduler.cancelDeadlineNotifications и оба уведомления отменяются.

## Callback-параметры

ONNAVIGATE - переход со стартового экрана на экран входа

ONTHIRDMAINSCREEN - переход на главный экран после входа

ONDOBAVITSOBITIE - переход на экран создания события

ONDOBAVITDOLSHNIKA - переход на экран добавления должника

ONPROFILE - переход на экран профиля

ONFRIENDS - переход на экран друзей

ONBACK - возврат на предыдущий экран через popBackStack

ONCREATESOBITIE - создаёт событие, добавляет в список sobitiya и планирует уведомления

ONADDDOLZHNIK - добавляет должника в список dolzhniki и планирует уведомления

ONTOGGLETHEME - переключает тему через ViewModel

ONDELETEDOLZHNIK - удаляет должника из списка и отменяет его уведомления

ONDELETESOBITIE - удаляет событие из списка и отменяет его уведомления

ONPAYDOLZHNIK - отмечает долг как оплаченный, удаляет должника и отменяет уведомления

ONPAYSOBITIE - отмечает событие как оплаченное, удаляет и отменяет уведомления

---

# 2. MainViewModel.kt

ViewModel приложения.
Хранит тему оформления и данные профиля пользователя.
Данные сохраняются в DataStore и живут между перезапусками приложения.

## Переменные

_ISDARKTHEME - приватный изменяемый поток состояния темы, по умолчанию false

ISDARKTHEME - публичный поток темы только для чтения, используется в UI

_USERNAME - приватный поток имени пользователя, по умолчанию "Пользователь"

USERNAME - публичный поток имени только для чтения

_USERPHOTOURI - приватный поток ссылки на фото профиля, по умолчанию null

USERPHOTOURI - публичный поток фото только для чтения

DARK_THEME_KEY - ключ типа Boolean для хранения темы в DataStore

USER_NAME_KEY - ключ типа String для хранения имени в DataStore

USER_PHOTO_KEY - ключ типа String для хранения ссылки на фото в DataStore

## Методы

TOGGLETHEME - переключает тему на противоположную в памяти без сохранения

LOADDARKTHEME(CONTEXT) - загружает тему, имя и фото из DataStore при запуске приложения

SAVEDARKTHEME(CONTEXT, VALUE) - сохраняет выбранную тему в DataStore

UPDATEPROFILE(CONTEXT, NAME, PHOTOURI) - обновляет имя и фото в памяти и сохраняет в DataStore. Если фото null - удаляет ключ из DataStore

---

# 3. FriendsViewModel.kt

ViewModel для работы с друзьями.
Хранит список всех пользователей, друзей, входящих и отправленных заявок.
База пользователей сейчас локальная, в будущем заменяется на сервер.

## Переменные

_ALLUSERS - приватный список всех пользователей, имитация базы данных

ALLUSERS - публичный поток всех пользователей только для чтения

_FRIENDS - приватный список текущих друзей пользователя

FRIENDS - публичный поток друзей только для чтения

_SENTREQUESTS - приватный список отправленных заявок которые ждут ответа

SENTREQUESTS - публичный поток отправленных заявок только для чтения

_INCOMINGREQUESTS - приватный список входящих заявок которые ждут моего ответа

INCOMINGREQUESTS - публичный поток входящих заявок только для чтения

## Методы

SEARCHUSERS(QUERY) - ищет пользователей по имени, фамилии, телефону или почте. Возвращает пустой список если запрос пустой. Исключает тех кто уже в друзьях или в заявках

SENDFRIENDREQUST(USER) - создаёт запись FriendRecord со статусом SENT и добавляет в список отправленных заявок

CANCELSENTREQUEST(USER) - удаляет пользователя из списка отправленных заявок по его id

ACCEPTINCOMINGREQUEST(USER) - удаляет пользователя из входящих заявок и добавляет в список друзей со статусом ACCEPTED

DECLINEINCOMINGREQUEST(USER) - удаляет пользователя из входящих заявок без добавления в друзья

REMOVEFRIEND(USER) - удаляет пользователя из списка друзей по его id

SIMULATEINCOMINGREQUEST - выбирает случайного пользователя из базы и добавляет входящую заявку со статусом PENDING. Проверяет что пользователь не задействован нигде

---

# 4. MainScreen.kt

Стартовый экран приложения.
Показывает название приложения, подзаголовок и кнопку "Начать".
При повторном запуске автоматически переходит на следующий экран через 2 секунды.

## Параметры функции

INNERPADDING - отступы от системных элементов

ISDARKTHEME - текущее состояние темы для выбора цветов фона и текста

ONNAVIGATE - callback перехода на следующий экран

## Переменные

PREFS - SharedPreferences с именем app_prefs, хранит факт первого запуска

ISFIRSTLAUNCH - true если приложение запускается впервые, читается из PREFS

VISIBLE - управляет видимостью элементов для анимации появления и исчезновения

SCALE - анимированное значение масштаба от 0.5 до 1.0 с длительностью 1000мс

GRADIENTCOLORS - список цветов для фона, зависит от ISDARKTHEME

TEXTCOLOR - цвет заголовка, зависит от ISDARKTHEME

SHADOWCOLOR - цвет тени под заголовком, зависит от ISDARKTHEME

SUBTITLECOLOR - цвет подзаголовка, зависит от ISDARKTHEME

## Логика

LaunchedEffect при старте устанавливает VISIBLE = true для анимации появления.
Если это не первый запуск - ждёт 2 секунды, скрывает элементы и вызывает ONNAVIGATE.
При нажатии кнопки "Начать" - сохраняет is_first_launch = false в SharedPreferences, скрывает элементы и через 500мс вызывает ONNAVIGATE.

## Элементы экрана

BOX - основной контейнер с вертикальным градиентным фоном, выравнивание по центру

COLUMN - размещает элементы по центру экрана

ANIMATEDVISIBILITY + TEXT "DELI" - название приложения с анимацией fadeIn 1500мс и анимацией масштаба через Modifier.scale

ANIMATEDVISIBILITY + TEXT "Делим расходы просто" - подзаголовок с анимацией fadeIn 2000мс

SPACER 16dp - отступ между заголовком и подзаголовком

SPACER 48dp - отступ перед кнопкой

ANIMATEDVISIBILITY + BUTTON "Начать" - кнопка видна только при первом запуске, анимация fadeIn 2500мс, ширина 60%

---

# 5. SecondScreen.kt

Экран входа и регистрации.
Можно переключаться между двумя режимами через кнопки.

## Параметры функции

INNERPADDING - отступы от системных элементов

ONTHIRDMAINSCREEN - callback перехода на главный экран

## Переменные

SCOPE - CoroutineScope для запуска сетевых запросов

ISREGISTRATION - режим экрана. true = регистрация, false = вход

FIRSTNAME - имя пользователя, обязательное поле

SECONDNAME - фамилия пользователя, обязательное поле

PHONE - номер телефона, обязательное поле

EMAIL - почта пользователя, обязательное поле только для регистрации

LINK - ссылка на перевод, необязательное поле только для регистрации

SHOWERROR - показывать ли сообщение об ошибке валидации

ISLOGINVALID - true если заполнены имя, фамилия и телефон

ISREGISTERVALID - true если заполнены имя, фамилия, телефон и почта

## Элементы экрана

BOX - корневой контейнер, размещает форму сверху и кнопку действия снизу

COLUMN - верхняя часть с заголовком и полями формы

TEXT "Регистрация" или "Вход" - заголовок экрана, меняется в зависимости от ISREGISTRATION

ROW - строка с двумя кнопками переключения режима

BUTTON "Регистрация" - активная кнопка в режиме регистрации

FILLEDTONALBUTTON "Регистрация" - неактивная кнопка переключения на регистрацию

BUTTON "Вход" - активная кнопка в режиме входа

FILLEDTONALBUTTON "Вход" - неактивная кнопка переключения на вход

OUTLINEDTEXTFIELD "Имя" - поле имени, обязательное, отображает ошибку если SHOWERROR и поле пустое

OUTLINEDTEXTFIELD "Фамилия" - поле фамилии, обязательное

OUTLINEDTEXTFIELD "Номер телефона" - поле телефона, обязательное, клавиатура Phone

OUTLINEDTEXTFIELD "Почта" - поле почты, обязательное только в режиме регистрации

OUTLINEDTEXTFIELD "Ссылка на перевод" - необязательное поле только в режиме регистрации

TEXT "Заполните все обязательные поля" - текст ошибки, цвет error, показывается если SHOWERROR = true

BUTTON "Зарегистрироваться" или "Войти" - нижняя кнопка действия

## Логика

При нажатии кнопки действия проверяется ISLOGINVALID или ISREGISTERVALID в зависимости от режима.
Если поля не заполнены - SHOWERROR = true и показывается сообщение об ошибке.
Если заполнены - переход на главный экран через ONTHIRDMAINSCREEN.

---

# 6. ThirdMainScreen.kt

Главный экран приложения.
Показывает список событий и должников через вкладки с перелистыванием.
Содержит карточки быстрых действий и кнопку перехода в профиль.

## Параметры функции

INNERPADDING - отступы от системных элементов

DOLZHNIKI - список должников для отображения

SOBITIYA - список событий для отображения

ONDOBAVITSOBITIE - переход на экран создания события

ONDOBAVITDOLSHNIKA - переход на экран добавления должника

ONPROFILE - переход на экран профиля

ONFRIENDS - переход на экран друзей

ONDELETEDOLZHNIK - удаление должника, принимает объект Dolzhnik

ONDELETESOBITIE - удаление события, принимает объект Sobitie

ONPAYDOLZHNIK - оплата долга, принимает объект Dolzhnik

ONPAYSOBITIE - оплата события, принимает объект Sobitie

## Переменные

DOLZHNIKTODELETE - должник выбранный для удаления. null означает что диалог закрыт

SOBITIETODELETE - событие выбранное для удаления. null означает что диалог закрыт

PAGERSTATE - состояние пейджера с двумя страницами

SCOPE - CoroutineScope для анимации переключения вкладок

TABS - список названий вкладок: "События" и "Должники"

## Элементы экрана

ALERTDIALOG "Удаление" - диалог подтверждения удаления должника
TEXT "Удалить должника ...?" - текст вопроса с именем должника
TEXTBUTTON "Удалить" - подтверждает удаление, цвет error
TEXTBUTTON "Отмена" - закрывает диалог без удаления

ALERTDIALOG "Удаление" - диалог подтверждения удаления события
TEXT "Удалить событие от ...?" - текст вопроса с датой события
TEXTBUTTON "Удалить" - подтверждает удаление, цвет error
TEXTBUTTON "Отмена" - закрывает диалог без удаления

COLUMN - основной вертикальный контейнер экрана

ROW - верхняя панель с названием и кнопкой профиля
TEXT "DELI" - название приложения, стиль headlineMedium, жирный
ICONBUTTON + ICON PERSON - кнопка перехода в профиль, цвет primary

SPACER 16dp - отступ после верхней панели

ROW - строка карточек быстрых действий
QUICKACTIONCARD "Событие" - переход на создание события, иконка Receipt
QUICKACTIONCARD "Должник" - переход на добавление должника, иконка PersonAdd
QUICKACTIONCARD "Друзья" - переход на экран друзей, иконка Group

SPACER 16dp - отступ перед вкладками

TABROW - строка вкладок "События" и "Должники"
TAB - каждая вкладка переключает страницу пейджера через animateScrollToPage
SecondaryIndicator - индикатор подчёркивания под активной вкладкой

SPACER 8dp - отступ после вкладок

HORIZONTALPAGER - контейнер с перелистыванием между страницами

Страница 0 - список событий
EMPTYPLACEHOLDER "Пока нет событий" - показывается если SOBITIYA пустой
LAZYCOLUMN - прокручиваемый список карточек событий
SOBITIECARD - карточка одного события

Страница 1 - список должников
EMPTYPLACEHOLDER "Пока нет должников" - показывается если DOLZHNIKI пустой
LAZYCOLUMN - прокручиваемый список карточек должников
DOLZHNIKCARD - карточка одного должника

## Вспомогательные компоненты

QUICKACTIONCARD - карточка быстрого действия. Принимает title, icon, onClick, modifier, isPrimary. Высота 90dp, скруглённые углы 16dp. Цвет фона зависит от isPrimary

EMPTYPLACEHOLDER - заглушка для пустого списка. Показывает текст по центру экрана

SOBITIECARD - карточка события. Показывает дату, количество участников, общую сумму, сумму на каждого. Содержит кнопку "Оплатить" и кнопку удаления с иконкой Delete

DOLZHNIKCARD - карточка должника. Показывает круглое фото или иконку Person, имя, дедлайн и сумму долга. Содержит кнопку "Оплатить" и кнопку удаления с иконкой Delete

## Расчёты в SobitieCard

EQUALSHARE = TOTALAMOUNT делить на количество участников

---

# 7. DobavitDolshnika.kt

Экран добавления нового должника.
Форма с полями: имя, сумма, дедлайн, фото.
Можно выбрать должника из списка друзей.

## Параметры функции

INNERPADDING - отступы от системных элементов

FRIENDS - список друзей для выбора должника

ONTHIRDMAINSCREEN - переход на главный экран после добавления

ONBACK - возврат на предыдущий экран

ONADDDOLZHNIK - callback добавления должника, принимает объект Dolzhnik

## Переменные

NAME - имя должника, строка

AMOUNT - сумма долга, строка

DEADLINETEXT - дата дедлайна в виде строки формата dd.MM.yyyy

SHOWDATEPICKER - открыт ли диалог выбора даты

PHOTOURI - URI выбранного фото. null если фото не выбрано

SHOWFRIENDSDIALOG - открыт ли диалог выбора должника из друзей

PHOTOLAUNCHER - лаунчер для выбора изображения из галереи через GetContent

## Элементы экрана

DATEPICKERDIALOG - диалог выбора даты дедлайна
DATEPICKER - календарь для выбора даты
TEXTBUTTON "OK" - подтверждает дату, форматирует в строку dd.MM.yyyy
TEXTBUTTON "Отмена" - закрывает календарь без выбора

ALERTDIALOG "Выбрать из друзей" - диалог со списком друзей
TEXT "У вас пока нет друзей" - показывается если FRIENDS пустой
LAZYCOLUMN - список карточек друзей
CARD - карточка друга. При нажатии подставляет имя и фото в форму и закрывает диалог
SURFACE + CIRCLESHAPE - круглый аватар друга
IMAGE - фото друга если есть
ICON PERSON - заглушка если фото нет
TEXT имя и фамилия - имя друга
TEXT телефон - номер телефона друга
TEXTBUTTON "Закрыть" - закрывает диалог

COLUMN - основной контейнер экрана с imePadding для отступа от клавиатуры

TEXT "Новый должник" - заголовок экрана, стиль headlineSmall, жирный

TEXT "Заполните данные о должнике" - подзаголовок, цвет onSurfaceVariant

COLUMN + VERTICALSCROLL - прокручиваемая область с полями формы

FILLEDTONALBUTTON "Выбрать из друзей" - открывает диалог выбора из друзей, иконка Group

OUTLINEDTEXTFIELD "Имя должника" - поле имени, singleLine

OUTLINEDTEXTFIELD "Сумма долга" - поле суммы, клавиатура Number, суффикс ₽, singleLine

OUTLINEDTEXTFIELD "Дедлайн" - поле даты, readOnly, singleLine, иконка CalendarMonth открывает DatePickerDialog

ROW - строка с кнопкой фото
FILLEDTONALBUTTON "Прикрепить фото" - открывает галерею через photoLauncher
TEXT "Фото выбрано" - показывается если PHOTOURI не null

IMAGE - превью выбранного фото. Высота 200dp, скруглённые углы 16dp. Показывается только если PHOTOURI не null

ROW - нижняя строка с кнопками действий
OUTLINEDBUTTON "Отмена" - возврат назад, высота 52dp
BUTTON "Добавить" - добавление должника, высота 52dp

## Логика

При нажатии "Добавить" проверяется что NAME и AMOUNT не пустые.
Если заполнены - создаётся объект Dolzhnik и вызывается ONADDDOLZHNIK.
Затем вызывается ONTHIRDMAINSCREEN для перехода на главный экран.
При выборе друга из диалога его имя подставляется в поле NAME, а фото в PHOTOURI.

---

# 8. DobavitSobitie.kt

Экран создания нового события.
Форма с датой, общей суммой, фото и списком участников.
Можно добавлять участников вручную или выбирать из списка друзей.

## Параметры функции

INNERPADDING - отступы от системных элементов

FRIENDS - список друзей для выбора участников

ONBACK - возврат на предыдущий экран

ONCREATESOBITIE - callback создания события, принимает объект Sobitie

## Переменные

PARTICIPANTS - список участников типа mutableStateListOf, изначально один пустой Participant

TOTALAMOUNT - общая сумма события в виде строки

SELECTEDDATE - выбранная дата в виде строки формата dd.MM.yyyy

SHOWDATEPICKER - открыт ли диалог выбора даты

PHOTOURI - URI выбранного фото. null если фото не выбрано

SHOWFRIENDSDIALOG - открыт ли диалог выбора участника из друзей

PHOTOLAUNCHER - лаунчер для выбора изображения из галереи через GetContent

TOTAL - значение TOTALAMOUNT преобразованное в Double через toDoubleOrNull

EQUALSHARE - равная доля на каждого участника. Считается как TOTAL делить на количество PARTICIPANTS

## Элементы экрана

DATEPICKERDIALOG - диалог выбора даты события
DATEPICKER - календарь для выбора даты
TEXTBUTTON "OK" - подтверждает дату, форматирует в dd.MM.yyyy
TEXTBUTTON "Отмена" - закрывает календарь без выбора

ALERTDIALOG "Выбрать из друзей" - диалог со списком друзей для добавления участника
TEXT "У вас пока нет друзей" - показывается если FRIENDS пустой
LAZYCOLUMN - список карточек друзей
CARD - карточка друга. При нажатии создаёт нового Participant с именем и телефоном и добавляет в PARTICIPANTS
SURFACE + CIRCLESHAPE - круглый аватар друга
IMAGE - фото друга если есть
ICON PERSON - заглушка если фото нет
TEXT имя и фамилия - имя друга
TEXT телефон - номер телефона друга
TEXTBUTTON "Закрыть" - закрывает диалог

COLUMN - основной контейнер экрана

TEXT "Создание события" - заголовок экрана, стиль headlineMedium, выравнивание по центру

LAZYCOLUMN - прокручиваемая форма с весом 1f

item - OUTLINEDTEXTFIELD "Дата события" - поле даты, readOnly. Иконка 📅 открывает DatePickerDialog

item - OUTLINEDTEXTFIELD "Общая сумма" - поле суммы, клавиатура Number

item - ROW с кнопкой фото
FILLEDTONALBUTTON "Прикрепить фото" - открывает галерею
TEXT "Фото выбрано" - показывается если PHOTOURI не null

item - IMAGE - превью выбранного фото. Высота 200dp, скруглённые углы 16dp. Показывается только если PHOTOURI не null

item - ROW "Участники" - заголовок блока участников с двумя кнопками
TEXT "Участники" - заголовок, стиль titleLarge
ICONBUTTON + ICON GROUP - открывает диалог выбора участника из друзей
ICONBUTTON + ICON ADD - добавляет пустой Participant в список

itemsIndexed PARTICIPANTS - список карточек участников
CARD - карточка одного участника, фон surfaceVariant
ROW - строка с номером участника и кнопкой удаления
TEXT "Участник N" - номер участника, стиль titleSmall
ICONBUTTON + ICON DELETE - удаляет участника из списка. Показывается только если участников больше одного
OUTLINEDTEXTFIELD "Имя" - поле имени участника
SPACER 8dp - отступ между полями
OUTLINEDTEXTFIELD "Номер телефона" - поле телефона, клавиатура Phone
SPACER 8dp - отступ между полями
OUTLINEDTEXTFIELD "Доп. сумма" - дополнительная сумма сверх равной доли, клавиатура Number, необязательное
SPACER 8dp - отступ перед расчётом
TEXT "Доля: ... руб" - показывает рассчитанную долю участника с учётом доп. суммы

item - CARD "Итого" - карточка итоговой информации, фон primaryContainer
TEXT "Итого" - заголовок, стиль titleMedium
TEXT "Общая сумма: ... руб" - общая сумма события
TEXT "На каждого поровну: ... руб" - равная доля на одного
TEXT "Сумма доплат: ... руб" - сумма всех дополнительных сумм
TEXT "Участников: N" - количество участников

SPACER 12dp - отступ перед нижними кнопками

ROW - нижняя строка с кнопками действий
OUTLINEDBUTTON "Назад" - возврат на предыдущий экран
BUTTON "Создать" - создание события

## Расчёты

TOTAL = TOTALAMOUNT.toDoubleOrNull или 0.0 если строка не число

EQUALSHARE = TOTAL делить на количество PARTICIPANTS. Если участников нет то 0.0

EXTRA = PARTICIPANT.EXTRAAMOUNT.toDoubleOrNull или 0.0 если строка не число

PERSONALTOTAL = EQUALSHARE плюс EXTRA - итоговая сумма для одного участника

EXTRASUM = сумма всех EXTRA всех участников - общая сумма доплат

## Логика

При нажатии "Создать" создаётся объект Sobitie с датой, суммой и текущим списком участников.
Вызывается ONCREATESOBITIE.
Затем вызывается ONBACK для возврата на предыдущий экран.
При выборе друга из диалога создаётся новый Participant с его именем и телефоном и добавляется в PARTICIPANTS.

---

# 9. Profile.kt

Экран профиля пользователя.
Показывает аватар, имя, статистику по долгам и событиям, настройки темы.
Имя и фото можно редактировать прямо на экране.

## Параметры функции

INNERPADDING - отступы от системных элементов

ONBACK - возврат на предыдущий экран

ISDARKTHEME - текущее состояние темы

ONTOGGLETHEME - переключение темы

DOLZHNIKI - список должников для расчёта статистики

SOBITIYA - список событий для расчёта статистики

USERNAME - имя пользователя из ViewModel

USERPHOTOURI - ссылка на фото профиля из ViewModel

ONUPDATEPROFILE - callback сохранения нового имени и фото, принимает String и String nullable

## Переменные

ISEDITING - режим редактирования профиля. true = режим редактирования включён

EDITEDNAME - временное имя при редактировании, инициализируется значением USERNAME

EDITEDPHOTOURI - временная ссылка на фото при редактировании, инициализируется значением USERPHOTOURI

PHOTOLAUNCHER - лаунчер для выбора фото из галереи через GetContent

## Элементы экрана

COLUMN - основной вертикальный контейнер экрана

BOX - центрирует аватар по горизонтали

BOX внутренний - содержит аватар и кнопку редактирования поверх него

SURFACE + CIRCLESHAPE - круглый контейнер аватара размером 96dp
В режиме редактирования добавляется Modifier.clickable для смены фото

IMAGE - фото профиля если EDITEDPHOTOURI не null. ContentScale.Crop

BOX + ICON PERSON - иконка заглушки если фото не выбрано. Размер 48dp

SURFACE + CIRCLESHAPE - маленькая кнопка 32dp поверх аватара в правом нижнем углу. Видна только в режиме редактирования
ICON EDIT - иконка карандаша для смены фото

Если ISEDITING = false:
ROW - строка с именем и кнопкой редактирования
TEXT - имя пользователя или "Пользователь" если пустое. Стиль headlineSmall, жирный
ICONBUTTON + ICON EDIT - переключает ISEDITING = true

Если ISEDITING = true:
OUTLINEDTEXTFIELD "Имя пользователя" - поле редактирования имени, singleLine

Если ISEDITING = true:
SPACER 12dp - отступ перед кнопками сохранения
ROW - строка с кнопками
OUTLINEDBUTTON "Отмена" - сбрасывает EDITEDNAME и EDITEDPHOTOURI к исходным значениям, выключает режим редактирования
BUTTON "Сохранить" - вызывает ONUPDATEPROFILE с новыми данными, выключает режим редактирования
ICON CHECK - иконка галочки внутри кнопки сохранения

SPACER 20dp - отступ перед статистикой

ROW - строка с двумя карточками статистики
STATCARD "События" - показывает количество событий из SOBITIYA.size
STATCARD "Должники" - показывает количество должников из DOLZHNIKI.size

SPACER 12dp - отступ перед карточкой суммы

CARD - карточка общей суммы долгов, фон primary
TEXT "Общая сумма долгов" - подпись, цвет onPrimary с прозрачностью 0.8
SPACER 4dp - отступ
TEXT "... руб" - итоговая сумма, стиль headlineMedium, жирный, цвет onPrimary

SPACER 16dp - отступ перед настройками

TEXT "Настройки" - заголовок блока настроек, стиль titleMedium, полужирный

SPACER 8dp - отступ

CARD - карточка настройки темы, фон surfaceContainerLow
ROW - строка с иконкой, текстом и переключателем
ICON DARKMODE - иконка тёмной темы, цвет primary
TEXT "Тёмная тема" - название настройки, стиль bodyLarge
SWITCH - переключатель темы. checked = ISDARKTHEME, вызывает ONTOGGLETHEME

SPACER weight 1f - заполняет оставшееся пространство, прижимает кнопку вниз

OUTLINEDBUTTON "Назад" - возврат на предыдущий экран. Ширина максимальная, высота 52dp

## Вспомогательный компонент STATCARD

Принимает value и label.
CARD - карточка со скруглёнными углами 16dp, фон secondaryContainer
COLUMN - вертикально размещает число и подпись
TEXT value - числовое значение, стиль headlineMedium, жирный, цвет onSecondaryContainer
TEXT label - подпись к числу, стиль bodyMedium, цвет onSecondaryContainer

## Расчёты

TOTALDEBT = сумма всех DOLZHNIK.AMOUNT через sumOf и toDoubleOrNull

---

# 10. FriendsScreen.kt

Экран управления друзьями.
Содержит четыре вкладки: поиск пользователей, список друзей, входящие заявки, отправленные заявки.

## Параметры функции

INNERPADDING - отступы от системных элементов

ONBACK - возврат на предыдущий экран

FRIENDS - список текущих друзей

SENTREQUESTS - список отправленных заявок

INCOMINGREQUESTS - список входящих заявок

ONSEARCH - поиск пользователей по строке запроса, возвращает список User

ONSENDREQUEST - отправка заявки в друзья, принимает User

ONCANCELREQUEST - отмена отправленной заявки, принимает User

ONACCEPTREQUEST - принятие входящей заявки, принимает User

ONDECLINEREQUEST - отклонение входящей заявки, принимает User

ONREMOVEFRIEND - удаление из друзей, принимает User

## Переменные

PAGERSTATE - состояние пейджера с четырьмя страницами

SCOPE - CoroutineScope для анимации переключения вкладок

TABS - список названий вкладок: "Поиск", "Друзья", "Входящие", "Отправл."

## Элементы FriendsScreen

COLUMN - основной вертикальный контейнер экрана

ROW - верхняя панель
ICONBUTTON + ICON ARROWBACK - возврат назад
SPACER 8dp - отступ
TEXT "Друзья" - заголовок экрана, стиль headlineSmall, жирный

SPACER 12dp - отступ перед вкладками

TABROW - строка из четырёх вкладок
SecondaryIndicator - индикатор подчёркивания под активной вкладкой
TAB - каждая вкладка переключает страницу через animateScrollToPage
Вкладка "Входящие" - если INCOMINGREQUESTS не пустой, показывает название и рядом красный кружок с количеством заявок

SPACER 12dp - отступ после вкладок

HORIZONTALPAGER - контейнер с перелистыванием
Страница 0 - SEARCHTAB
Страница 1 - FRIENDSTAB
Страница 2 - INCOMINGREQUESTSTAB
Страница 3 - SENTREQUESTSTAB

## Вкладка SearchTab

QUERY - строка поискового запроса

RESULTS - список найденных пользователей

SENTTOIDS - локальный список id пользователей которым уже отправлена заявка в текущей сессии

LAUNCHEDEFFECT(QUERY) - при каждом изменении QUERY вызывается ONSEARCH и обновляет RESULTS

COLUMN - контейнер вкладки

OUTLINEDTEXTFIELD "Имя, фамилия, телефон или почта" - поле ввода запроса, иконка Search, singleLine

SPACER 12dp - отступ

EMPTYMESSAGE "Введите имя, телефон или почту" - показывается если QUERY пустой

EMPTYMESSAGE "Никого не найдено" - показывается если RESULTS пустой при непустом запросе

LAZYCOLUMN - список найденных пользователей
USERCARD - карточка пользователя с кнопкой действия
Если id пользователя есть в SENTTOIDS - кнопка "Отправлено" неактивная с иконкой Check
Если нет - кнопка "Добавить" с иконкой PersonAdd. При нажатии вызывает ONSENDREQUEST и добавляет id в SENTTOIDS

## Вкладка FriendsTab

EMPTYMESSAGE "У вас пока нет друзей" - если FRIENDS пустой

LAZYCOLUMN - список карточек друзей
USERCARD - карточка друга
FILLEDTONALICONBUTTON + ICON CLOSE - кнопка удаления из друзей, фон errorContainer, иконка цвет error

## Вкладка IncomingRequestsTab

EMPTYMESSAGE "Нет входящих заявок" - если REQUESTS пустой

LAZYCOLUMN - список входящих заявок
USERCARD - карточка заявки
ROW - строка с двумя кнопками
FILLEDICONBUTTON + ICON CHECK - принимает заявку, вызывает ONACCEPT
FILLEDTONALICONBUTTON + ICON CLOSE - отклоняет заявку, вызывает ONDECLINE, фон errorContainer

## Вкладка SentRequestsTab

EMPTYMESSAGE "Нет отправленных заявок" - если REQUESTS пустой

LAZYCOLUMN - список отправленных заявок
CARD - карточка отправленной заявки
USERCARDCONTENT - блок с данными пользователя
SPACER 8dp - отступ
TEXT "Ожидает ответа" - статус заявки, цвет onSurfaceVariant
SPACER 8dp - отступ
FILLEDTONALBUTTON "Отменить заявку" - отменяет заявку, вызывает ONCANCEL

## Вспомогательные компоненты

USERCARD - универсальная карточка пользователя
Принимает user и actionButton как composable параметр
CARD - карточка с фоном surfaceContainerLow, скруглённые углы 16dp
ROW - размещает USERCARDCONTENT и actionButton в строку

USERCARDCONTENT - блок с аватаром и текстовыми данными пользователя
ROW - строка с аватаром и текстом
SURFACE + CIRCLESHAPE - круглый аватар 48dp
IMAGE - фото пользователя если PHOTOURI не null
ICON PERSON - заглушка если фото нет. Размер 24dp
COLUMN - блок с текстом
TEXT имя и фамилия - стиль titleMedium, жирный
TEXT телефон - стиль bodySmall, цвет onSurfaceVariant
TEXT почта - стиль bodySmall, цвет onSurfaceVariant

EMPTYMESSAGE - заглушка для пустого раздела
BOX - занимает весь экран, выравнивание по центру
TEXT - текст заглушки, стиль bodyLarge, цвет onSurfaceVariant

---

# 11. Уведомления

## NotificationHelper.kt

Объект для создания канала уведомлений и показа уведомлений.

CHANNEL_ID - строковый идентификатор канала "deli_deadline_channel"

CHANNEL_NAME - название канала "Дедлайны"

CREATECHANNEL(CONTEXT) - создаёт канал уведомлений с высоким приоритетом IMPORTANCE_HIGH. Работает только на Android 8 и выше

SHOWNOTIFICATION(CONTEXT, NOTIFICATIONID, TITLE, MESSAGE) - собирает уведомление через NotificationCompat.Builder. Устанавливает иконку, заголовок, текст, стиль BigText для длинных сообщений, приоритет HIGH. Добавляет PendingIntent для открытия MainActivity по нажатию. Устанавливает autoCancel = true чтобы уведомление исчезало после нажатия

## NotificationReceiver.kt

BroadcastReceiver который получает сигнал от AlarmManager в нужное время.

ONRECEIVE(CONTEXT, INTENT) - вызывается системой когда срабатывает будильник. Извлекает title из интента или подставляет "Дедлайн". Извлекает message или подставляет "Скоро дедлайн". Извлекает notificationId или подставляет 0. Вызывает createChannel и showNotification

## NotificationScheduler.kt

Объект для планирования и отмены уведомлений о дедлайнах.

SCHEDULEDEADLINENOTIFICATIONS(CONTEXT, TITLE, MESSAGE, DEADLINE, BASEID) - парсит строку даты в формате dd.MM.yyyy. Устанавливает время на 10 утра. Считает время за неделю = deadlineMillis минус 7 дней. Считает время за день = deadlineMillis минус 1 день. Планирует уведомление за неделю с id BASEID плюс 1. Планирует уведомление за день с id BASEID плюс 2. Каждое уведомление планируется только если его время ещё не прошло

SCHEDULEALARM(CONTEXT, TRIGGERAT, TITLE, MESSAGE, NOTIFICATIONID) - создаёт Intent для NotificationReceiver с данными уведомления. Оборачивает в PendingIntent через getBroadcast. На Android 12 и выше проверяет canScheduleExactAlarms. Если разрешение есть - использует setExactAndAllowWhileIdle. Если нет - использует обычный set. На Android ниже 12 всегда использует setExactAndAllowWhileIdle

CANCELDEADLINENOTIFICATIONS(CONTEXT, BASEID) - отменяет уведомления с id BASEID плюс 1 и BASEID плюс 2. Для каждого ищет существующий PendingIntent через FLAG_NO_CREATE. Если найден - отменяет через alarmManager.cancel

---

# 12. Модели данных

## Dolzhnik

Файл Dolzhnik.kt
Модель одного должника.

NAME - имя должника, тип String

AMOUNT - сумма долга, тип String

DEADLINE - дата дедлайна, тип String

PHOTOURI - ссылка на фото, тип String nullable, по умолчанию null

## Participant

Файл SobitieModels.kt
Модель одного участника события.

NAME - имя участника, тип String, по умолчанию пустая строка

PHONE - номер телефона, тип String, по умолчанию пустая строка

EXTRAAMOUNT - дополнительная сумма сверх равной доли, тип String, по умолчанию пустая строка

## Sobitie

Файл SobitieModels.kt
Модель одного события.

DATE - дата события, тип String

TOTALAMOUNT - общая сумма события, тип Double

PARTICIPANTS - список участников, тип List of Participant

## User

Файл User.kt
Модель пользователя для системы друзей.

ID - уникальный идентификатор, тип String

FIRSTNAME - имя, тип String

LASTNAME - фамилия, тип String

PHONE - номер телефона, тип String

EMAIL - адрес электронной почты, тип String

PHOTOURI - ссылка на фото, тип String nullable, по умолчанию null

## FriendRecord

Файл User.kt
Модель записи о друге или заявке.

USER - данные пользователя, тип User

STATUS - статус отношений, тип FriendRequestStatus

## FriendRequestStatus

Файл User.kt
Перечисление статусов заявки в друзья.

PENDING - входящая заявка ожидает моего ответа

SENT - заявка отправлена и ожидает ответа другого пользователя

ACCEPTED - заявка принята, пользователь добавлен в друзья

---

# 13. Структура файлов проекта

app/src/main/java/com/example/deli/

MainActivity.kt - навигация и подключение ViewModel и уведомлений

MainScreen.kt - стартовый экран с анимацией и определением первого запуска

SecondScreen.kt - экран входа и регистрации

MainViewModel.kt - хранение темы и данных профиля через DataStore

FriendsViewModel.kt - логика работы с друзьями, поиском и заявками

notifications/

NotificationHelper.kt - создание канала и показ уведомлений

NotificationReceiver.kt - получение сигнала от AlarmManager

NotificationScheduler.kt - планирование и отмена уведомлений

ui/theme/

ThirdMainScreen.kt - главный экран со вкладками и быстрыми действиями

DobavitDolshnika.kt - экран добавления должника с выбором из друзей

DobavitSobitie.kt - экран создания события с участниками и расчётами

Profile.kt - экран профиля с редактированием и статистикой

FriendsScreen.kt - экран друзей с четырьмя вкладками

Dolzhnik.kt - модель должника

SobitieModels.kt - модели Participant и Sobitie

User.kt - модели User, FriendRecord и FriendRequestStatus

Theme.kt - настройки темы Compose с поддержкой динамических цветов

Color.kt - цвета приложения для светлой и тёмной темы

Type.kt - стили текста

---

# 14. Compose-элементы (общая справка)

## Отображение

TEXT - блок для вывода текста на экран

IMAGE - отображение картинки из ресурсов или по URI

ICON - отображение векторной иконки

SPACER - пустой отступ между элементами

## Кнопки

BUTTON - основная кнопка действия, заливной фон цвета primary

OUTLINEDBUTTON - контурная кнопка без заливки

FILLEDTONALBUTTON - кнопка с мягким вторичным фоном

TEXTBUTTON - текстовая кнопка без фона и границ

ICONBUTTON - кнопка с иконкой без фона

FILLEDICONBUTTON - заливная кнопка только с иконкой

FILLEDTONALICONBUTTON - кнопка с иконкой и мягким фоном, можно задать цвет контейнера

## Поля ввода

OUTLINEDTEXTFIELD - поле ввода текста с контуром, поддерживает label, leadingIcon, trailingIcon, suffix, readOnly, singleLine, keyboardOptions

## Контейнеры

COLUMN - размещает дочерние элементы вертикально сверху вниз

ROW - размещает дочерние элементы горизонтально слева направо

BOX - размещает дочерние элементы поверх друг друга

LAZYCOLUMN - прокручиваемый список, рендерит только видимые элементы

SCAFFOLD - каркас экрана с поддержкой отступов от системных элементов

CARD - карточка для группировки контента с тенью и фоном

SURFACE - контейнер с цветом, формой и возможностью клика

## Диалоги

ALERTDIALOG - всплывающее модальное окно с заголовком, текстом и кнопками

DATEPICKERDIALOG - модальное окно с календарём для выбора даты

DATEPICKER - сам календарь, используется внутри DatePickerDialog

## Навигация и вкладки

TABROW - строка вкладок с индикатором активной

TAB - одна вкладка в строке

HORIZONTALPAGER - контейнер с горизонтальным перелистыванием страниц

## Анимация

ANIMATEDVISIBILITY - плавное появление и скрытие элемента с поддержкой enter и exit анимаций

ANIMATEFLOATASTATE - анимирует числовое значение Float при его изменении

FADEIN - анимация появления через прозрачность, принимает animationSpec

FADEOUT - анимация исчезновения через прозрачность, принимает animationSpec

TWEEN - спецификация анимации с заданной длительностью в миллисекундах

## Переключатели

SWITCH - переключатель с двумя состояниями true и false

---

# 15. Modifier (настройка элементов)

FILLMAXSIZE - элемент занимает весь доступный размер по ширине и высоте

FILLMAXWIDTH - элемент занимает всю доступную ширину

FILLMAXWIDTH(0.6f) - элемент занимает 60% доступной ширины

PADDING(24.dp) - отступ со всех четырёх сторон

PADDING(horizontal = 16.dp, vertical = 12.dp) - отступы по горизонтали и вертикали отдельно

HEIGHT(200.dp) - фиксированная высота элемента

SIZE(48.dp) - фиксированный размер по ширине и высоте

WEIGHT(1f) - элемент занимает пропорциональную долю свободного места в Row или Column

CLIP(CircleShape) - обрезает элемент в форму круга

CLIP(RoundedCornerShape(16.dp)) - обрезает углы элемента со скруглением 16dp

SCALE(value) - масштабирует элемент относительно его центра

ALIGN(Alignment.CenterHorizontally) - выравнивание по горизонтальному центру внутри Column

IMEPADDING - добавляет отступ снизу равный высоте клавиатуры

CLICKABLE - делает элемент реагирующим на нажатие

---

# 16. Стили Material 3

## Типография

MATERIALTHEME.TYPOGRAPHY.HEADLINEMEDIUM - крупный заголовок экрана

MATERIALTHEME.TYPOGRAPHY.HEADLINESMALL - заголовок чуть меньше

MATERIALTHEME.TYPOGRAPHY.TITLELARGE - крупный подзаголовок

MATERIALTHEME.TYPOGRAPHY.TITLEMEDIUM - средний подзаголовок

MATERIALTHEME.TYPOGRAPHY.TITLESMALL - мелкий подзаголовок

MATERIALTHEME.TYPOGRAPHY.BODYLARGE - основной текст крупный

MATERIALTHEME.TYPOGRAPHY.BODYMEDIUM - основной текст средний

MATERIALTHEME.TYPOGRAPHY.BODYSMALL - основной текст мелкий, подсказки

MATERIALTHEME.TYPOGRAPHY.LABELSMALL - очень мелкий текст для меток и счётчиков

## Цвета

MATERIALTHEME.COLORSCHEME.PRIMARY - основной акцентный цвет приложения

MATERIALTHEME.COLORSCHEME.ONPRIMARY - цвет текста и иконок на фоне primary

MATERIALTHEME.COLORSCHEME.PRIMARYCONTAINER - светлый фон для выделенных элементов

MATERIALTHEME.COLORSCHEME.ONPRIMARYCONTAINER - цвет текста на фоне primaryContainer

MATERIALTHEME.COLORSCHEME.SECONDARY - вторичный цвет приложения

MATERIALTHEME.COLORSCHEME.SECONDARYCONTAINER - вторичный фон для карточек

MATERIALTHEME.COLORSCHEME.ONSECONDARYCONTAINER - цвет текста на вторичном фоне

MATERIALTHEME.COLORSCHEME.ERROR - цвет ошибки, обычно красный

MATERIALTHEME.COLORSCHEME.ERRORCONTAINER - светлый фон для элементов с ошибкой

MATERIALTHEME.COLORSCHEME.SURFACE - фон поверхностей и экранов

MATERIALTHEME.COLORSCHEME.SURFACEVARIANT - альтернативный фон поверхности

MATERIALTHEME.COLORSCHEME.SURFACECONTAINERLOW - фон карточек чуть темнее поверхности

MATERIALTHEME.COLORSCHEME.ONSURFACEVARIANT - цвет вспомогательного текста и подсказок

---

# 17. DataStore

Используется для хранения данных между перезапусками приложения.
Данные хранятся в файле на устройстве и читаются асинхронно.

CONTEXT.DATASTORE - экземпляр DataStore подключённый через extension к контексту

DARK_THEME_KEY = booleanPreferencesKey("dark_theme") - ключ для булевого значения темы

USER_NAME_KEY = stringPreferencesKey("user_name") - ключ для строки имени пользователя

USER_PHOTO_KEY = stringPreferencesKey("user_photo") - ключ для строки ссылки на фото

DATASTORE.DATA.FIRST() - одноразовое синхронное чтение текущих данных внутри корутины

DATASTORE.EDIT { prefs -> } - запись или изменение данных внутри транзакции

PREFS[KEY] = VALUE - установить значение по ключу

PREFS.REMOVE(KEY) - удалить значение по ключу

---

# 18. Уведомления (общая справка)

NOTIFICATIONHELPER.CREATECHANNEL - создаёт канал уведомлений, вызывается один раз при старте

NOTIFICATIONHELPER.SHOWNOTIFICATION - немедленно показывает уведомление с заданными данными

NOTIFICATIONSCHEDULER.SCHEDULEDEADLINENOTIFICATIONS - планирует два уведомления: за неделю до дедлайна с id BASEID + 1, и за день до дедлайна с id BASEID + 2. Уведомления срабатывают в 10 утра

NOTIFICATIONSCHEDULER.CANCELDEADLINENOTIFICATIONS - отменяет оба уведомления по BASEID

NOTIFICATIONRECEIVER - получает сигнал от AlarmManager и вызывает showNotification

ALARMMANAGER - системный планировщик задач Android

PENDINGINTENT - упакованный интент для запуска из AlarmManager или по нажатию на уведомление

BASEID - уникальный числовой идентификатор, вычисляется через hashCode объекта Dolzhnik или Sobitie