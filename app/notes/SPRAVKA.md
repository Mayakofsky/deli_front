# Справка по проекту DELI

---

# 1. MainActivity.kt

Главный файл приложения.
Здесь создаётся навигация между экранами и подключается ViewModel.

## Переменные

VIEWMODEL — главный ViewModel, хранит все данные приложения
CONTEXT — контекст приложения, нужен для DataStore
ISDARKTHEME — текущее состояние темы (true = тёмная, false = светлая)
DOLZHNIKI — список всех должников, приходит из ViewModel
SOBITIYA — список всех событий, приходит из ViewModel
NAVCONTROLLER — контроллер навигации, управляет переходами между экранами
INNERPADDING — отступы от системных элементов (статус-бар, навигация)

## Навигация

screen_1 — стартовый экран (MainScreen)
screen_2 — экран входа и регистрации (SecondScreen)
screen_3 — главный экран приложения (ThirdMainScreen)
screen_5 — экран добавления должника (DobavitDolshnika)
screen_6 — экран профиля (Profile)
screen_7 — экран создания события (DobavitSobitie)

## Callback-параметры

ONNAVIGATE — переход со стартового экрана на экран входа
ONTHIRDMAINSCREEN — переход на главный экран после входа
ONDOBAVITSOBITIE — переход на экран создания события
ONDOBAVITDOLSHNIKA — переход на экран добавления должника
ONPROFILE — переход на экран профиля
ONBACK — возврат на предыдущий экран
ONCREATESOBITIE — создание события и добавление в список
ONADDDOLZHNIK — добавление должника в список
ONTOGGLETHEME — переключение темы
ONDELETEDOLZHNIK — удаление должника из списка
ONDELETESOBITIE — удаление события из списка

---

# 2. MainViewModel.kt

ViewModel приложения.
Хранит все данные и бизнес-логику.
Данные живут пока приложение открыто.

## Переменные

_DOLZHNIKI — приватный изменяемый список должников
DOLZHNIKI — публичный список должников для UI
_SOBITIYA — приватный изменяемый список событий
SOBITIYA — публичный список событий для UI
_ISDARKTHEME — приватное состояние темы
ISDARKTHEME — публичное состояние темы для UI

## Методы

ADDDOLZHNIK(DOLZHNIK) — добавляет должника в список
REMOVEDOLZHNIK(DOLZHNIK) — удаляет должника из списка
ADDSOBITIE(SOBITIE) — добавляет событие в список
REMOVESOBITIE(SOBITIE) — удаляет событие из списка
TOGGLETHEME() — переключает тему в памяти
SAVEDARKTHEME(CONTEXT, ISDARK) — сохраняет тему в DataStore
LOADDARKTHEME(CONTEXT) — загружает тему из DataStore

---

# 3. ThemePreferences.kt

Объект для сохранения темы на устройстве.
Использует DataStore.

## Переменные

DARK_THEME_KEY — ключ для хранения значения темы
CONTEXT.DATASTORE — DataStore приложения

## Методы

ISDARKTHEME(CONTEXT) — читает сохранённую тему, возвращает Flow<Boolean>
SAVEDARKTHEME(CONTEXT, ISDARK) — сохраняет тему

---

# 4. MainScreen.kt

Стартовый экран приложения.
Показывает название и кнопку "Начать".

## Параметры функции

INNERPADDING — отступы от системных элементов
ONNAVIGATE — callback перехода на следующий экран

## Элементы экрана

COLUMN — основной контейнер, размещает элементы по центру
TEXT "DELI" — название приложения, стиль displayMedium, цвет primary
SPACER 8dp — отступ между текстами
TEXT "Делим расходы просто" — описание, стиль bodyLarge, цвет onSurfaceVariant
SPACER 48dp — отступ перед кнопкой
BUTTON "Начать" — переход на экран входа, ширина 80%

---

# 5. SecondScreen.kt

Экран входа и регистрации.
Можно переключаться между двумя режимами.

## Параметры функции

INNERPADDING — отступы от системных элементов
ONTHIRDMAINSCREEN — callback перехода на главный экран

## Переменные

SCOPE — CoroutineScope для сетевых запросов
ISREGISTRATION — режим экрана: true = регистрация, false = вход
FIRSTNAME — имя пользователя
SECONDNAME — фамилия пользователя
PHONE — номер телефона
EMAIL — почта (только для регистрации)
LINK — ссылка на перевод (необязательное поле, только для регистрации)
SHOWERROR — показывать ли ошибки валидации
ISLOGINVALID — true если заполнены: имя, фамилия, телефон
ISREGISTERVALID — true если заполнены: имя, фамилия, телефон, почта

## Элементы экрана

BOX — корневой контейнер с кнопкой внизу
COLUMN — верхняя часть с формой
TEXT "Регистрация" / "Вход" — заголовок, меняется в зависимости от режима
ROW — кнопки переключения режима
FILLEDTONALBUTTON "Вход" — неактивная кнопка переключения на вход
BUTTON "Регистрация" — активная кнопка регистрации
BUTTON "Вход" — активная кнопка входа
FILLEDTONALBUTTON "Регистрация" — неактивная кнопка переключения на регистрацию
OUTLINEDTEXTFIELD "Имя *" — поле имени, обязательное
OUTLINEDTEXTFIELD "Фамилия *" — поле фамилии, обязательное
OUTLINEDTEXTFIELD "Номер телефона *" — поле телефона, обязательное
OUTLINEDTEXTFIELD "Почта *" — поле почты, обязательное (только регистрация)
OUTLINEDTEXTFIELD "Ссылка на перевод" — необязательное поле (только регистрация)
TEXT "Заполните все обязательные поля" — ошибка, цвет error
BUTTON "Зарегистрироваться" / "Войти" — нижняя кнопка действия

## Логика

При нажатии кнопки проверяется ISLOGINVALID или ISREGISTERVALID.
Если поля не заполнены — SHOWERROR = true, показываются ошибки.
Если заполнены — переход на главный экран.
При регистрации дополнительно вызывается RETROFITCLIENT.APISERVICE.ADDUSER().

---

# 6. ThirdMainScreen.kt

Главный экран приложения.
Показывает список событий и должников.
Есть кнопки перехода на другие экраны.

## Параметры функции

INNERPADDING — отступы от системных элементов
DOLZHNIKI — список должников
SOBITIYA — список событий
ONDOBAVITSOBITIE — переход на создание события
ONDOBAVITDOLSHNIKA — переход на добавление должника
ONPROFILE — переход в профиль
ONDELETEDOLZHNIK — удаление должника
ONDELETESOBITIE — удаление события

## Переменные

DOLZHNIKTODELETE — должник, выбранный для удаления (null = диалог закрыт)
SOBITIETODELETE — событие, выбранное для удаления (null = диалог закрыт)

## Элементы экрана

ALERTDIALOG "Удаление должника" — диалог подтверждения удаления должника
BUTTON "Удалить" — подтверждение удаления
TEXTBUTTON "Отмена" — отмена удаления

ALERTDIALOG "Удаление события" — диалог подтверждения удаления события
BUTTON "Удалить" — подтверждение удаления
TEXTBUTTON "Отмена" — отмена удаления

COLUMN — основной контейнер экрана
TEXT "Главная" — заголовок экрана
BUTTON "Сформировать событие" — переход на создание события
FILLEDTONALBUTTON "Добавить должника" — переход на добавление должника
OUTLINEDBUTTON "Профиль" — переход в профиль

LAZYCOLUMN — прокручиваемый список
TEXT "События" — заголовок блока событий
TEXT "Пока нет событий" — если список пуст
ITEMS(SOBITIYA) — список карточек событий
CARD — карточка одного события
ROW — верхняя строка: дата + кнопка удаления
TEXT "Дата: ..." — дата события
ICONBUTTON DELETE — кнопка удаления события
TEXT "Сумма: ..." — общая сумма
TEXT "Участников: ..." — количество участников
TEXT "На каждого: ..." — сумма на одного
FOREACH PARTICIPANTS — список участников
TEXT "Имя: сумма" — имя и доля каждого участника

TEXT "Должники" — заголовок блока должников
TEXT "Пока нет должников" — если список пуст
ITEMS(DOLZHNIKI) — список карточек должников
CARD — карточка одного должника
ROW — верхняя строка: имя + кнопка удаления
TEXT "Имя" — имя должника
ICONBUTTON DELETE — кнопка удаления должника
TEXT "Сумма: ..." — сумма долга
TEXT "Дедлайн: ..." — дата дедлайна

## Расчёты

EQUALSHARE = TOTALAMOUNT / количество участников
EXTRA = дополнительная сумма участника
PERSONALTOTAL = EQUALSHARE + EXTRA

---

# 7. DobavitDolshnika.kt

Экран добавления должника.
Форма с полями: имя, сумма, дедлайн.

## Параметры функции

INNERPADDING — отступы от системных элементов
ONTHIRDMAINSCREEN — переход на главный экран после добавления
ONBACK — возврат назад
ONADDDOLZHNIK — callback добавления должника

## Переменные

NAME — имя должника
AMOUNT — сумма долга
DEADLINETEXT — дата дедлайна в виде строки
SHOWDATEPICKER — открыт ли календарь

## Элементы экрана

DATEPICKERDIALOG — диалог выбора даты
DATEPICKER — календарь
TEXTBUTTON "OK" — подтверждение даты
TEXTBUTTON "Отмена" — закрытие календаря

COLUMN — основной контейнер экрана
TEXT "Добавление должника" — заголовок экрана

LAZYCOLUMN — область формы
CARD — карточка с полями
OUTLINEDTEXTFIELD "Имя должника" — поле имени
OUTLINEDTEXTFIELD "Сумма денег" — поле суммы, клавиатура Number
OUTLINEDTEXTFIELD "Дедлайн" — поле даты, readOnly, иконка 📅

ROW — нижние кнопки
OUTLINEDBUTTON "Назад" — возврат назад
BUTTON "Добавить" — добавление должника

## Логика

При нажатии "Добавить" проверяется что NAME и AMOUNT не пустые.
Если заполнены — создаётся DOLZHNIK и вызывается ONADDDOLZHNIK.
Затем переход на главный экран.

---

# 8. DobavitSobitie.kt

Экран создания события.
Форма с датой, суммой, фото и списком участников.

## Параметры функции

INNERPADDING — отступы от системных элементов
ONBACK — возврат назад
ONCREATESOBITIE — callback создания события

## Переменные

PARTICIPANTS — список участников (изначально один пустой)
TOTALAMOUNT — общая сумма в виде строки
SELECTEDDATE — выбранная дата в виде строки
SHOWDATEPICKER — открыт ли календарь
PHOTOURI — URI выбранного фото (null = фото не выбрано)
PHOTOLAUNCHER — лаунчер для выбора фото из галереи
TOTAL — общая сумма как число Double
EQUALSHARE — сумма на одного участника

## Элементы экрана

DATEPICKERDIALOG — диалог выбора даты события
DATEPICKER — календарь
TEXTBUTTON "OK" — подтверждение даты
TEXTBUTTON "Отмена" — закрытие календаря

COLUMN — основной контейнер экрана
TEXT "Создание события" — заголовок экрана

LAZYCOLUMN — прокручиваемая форма
OUTLINEDTEXTFIELD "Дата события" — поле даты, readOnly, иконка 📅
OUTLINEDTEXTFIELD "Общая сумма" — поле суммы, клавиатура Number

ROW — блок фото
FILLEDTONALBUTTON "Прикрепить фото" — открывает галерею
TEXT "Фото выбрано ✓" — показывается если фото выбрано

IMAGE — превью выбранного фото (если PHOTOURI != null)

ROW — заголовок участников
TEXT "Участники" — заголовок блока
ICONBUTTON ADD — кнопка добавления нового участника

ITEMSINDEXED(PARTICIPANTS) — список карточек участников
CARD — карточка одного участника
ROW — верхняя строка: номер + кнопка удаления
TEXT "Участник N" — номер участника
ICONBUTTON DELETE — удаление участника (если их больше одного)
OUTLINEDTEXTFIELD "Имя" — имя участника
OUTLINEDTEXTFIELD "Номер телефона" — телефон, клавиатура Phone
OUTLINEDTEXTFIELD "Доп. сумма" — дополнительная сумма, клавиатура Number
TEXT "Доля: ... ₽" — рассчитанная доля участника

CARD "Итого" — карточка итогов
TEXT "Общая сумма: ..." — общая сумма события
TEXT "На каждого поровну: ..." — сумма на одного
TEXT "Сумма доплат: ..." — сумма всех дополнительных сумм
TEXT "Участников: ..." — количество участников

ROW — нижние кнопки
OUTLINEDBUTTON "Назад" — возврат назад
BUTTON "Создать" — создание события

## Расчёты

TOTAL = TOTALAMOUNT.TODOUBLEORNULL() — преобразование строки в число
EQUALSHARE = TOTAL / количество участников — доля каждого
EXTRA = PARTICIPANT.EXTRAAMOUNT.TODOUBLEORNULL() — доп. сумма участника
PERSONALTOTAL = EQUALSHARE + EXTRA — итого для участника
EXTRASUM = сумма всех EXTRA — общая сумма доплат

## Логика

При нажатии "Создать" создаётся SOBITIE с датой, суммой и списком участников.
Вызывается ONCREATESOBITIE.
Затем возврат на предыдущий экран.

---

# 9. Profile.kt

Экран профиля.
Показывает статистику и настройки.

## Параметры функции

INNERPADDING — отступы от системных элементов
ONBACK — возврат назад
ISDARKTHEME — текущее состояние темы
ONTOGGLETHEME — переключение темы
DOLZHNIKI — список должников для статистики
SOBITIYA — список событий для статистики

## Элементы экрана

COLUMN — основной контейнер экрана
TEXT "Профиль" — заголовок экрана

LAZYCOLUMN — прокручиваемая область
CARD "Статистика" — карточка с фоном primaryContainer
TEXT "Статистика" — заголовок блока
TEXT "Событий: N" — количество событий
TEXT "Должников: N" — количество должников
TEXT "Общая сумма долгов: N ₽" — сумма всех долгов
TEXT "Всего участников: N" — сумма участников во всех событиях

CARD "Настройки" — карточка с фоном surfaceVariant
TEXT "Настройки" — заголовок блока
ROW — строка с переключателем темы
TEXT "Тёмная тема" / "Светлая тема" — текст зависит от ISDARKTHEME
SWITCH — переключатель темы

OUTLINEDBUTTON "Назад" — возврат на предыдущий экран

## Расчёты

TOTALDEBT = сумма всех DOLZHNIK.AMOUNT — общая сумма долгов
TOTALEVENTS = SOBITIYA.SIZE — количество событий
TOTALPARTICIPANTS = сумма SOBITIE.PARTICIPANTS.SIZE — всего участников

---

# 10. Модели данных

## Dolzhnik (файл Dolzhnik.kt)

Модель должника.

NAME — имя должника (String)
AMOUNT — сумма долга (String)
DEADLINE — дата дедлайна (String)

## Participant (файл SobitieModels.kt)

Модель участника события.

NAME — имя участника (String, по умолчанию "")
PHONE — номер телефона (String, по умолчанию "")
EXTRAAMOUNT — дополнительная сумма (String, по умолчанию "")

## Sobitie (файл SobitieModels.kt)

Модель события.

DATE — дата события (String)
TOTALAMOUNT — общая сумма (Double)
PARTICIPANTS — список участников (List<Participant>)

---

# 11. Структура файлов проекта

app/src/main/java/com/example/deli/
├── MainActivity.kt — навигация и подключение ViewModel
├── MainScreen.kt — стартовый экран
├── SecondScreen.kt — экран входа / регистрации
├── MainViewModel.kt — хранение данных и логика
├── ThemePreferences.kt — сохранение темы в DataStore
├── RetrofitClient.kt — подключение к серверу
├── ApiService.kt — интерфейс API
│
├── ui/theme/
│ ├── ThirdMainScreen.kt — главный экран со списками
│ ├── DobavitDolshnika.kt — экран добавления должника
│ ├── DobavitSobitie.kt — экран создания события
│ ├── Profile.kt — экран профиля
│ ├── Dolzhnik.kt — модель должника
│ ├── SobitieModels.kt — модели Participant и Sobitie
│ ├── Theme.kt — настройки темы Compose
│ ├── Color.kt — цвета приложения
│ └── Type.kt — стили текста


---

# 12. Compose-элементы (общая справка)

## Отображение

TEXT() — блок для вывода текста
IMAGE() — отображение картинки
ICON() — иконка
SPACER() — пустой отступ

## Кнопки

BUTTON() — основная кнопка действия
OUTLINEDBUTTON() — контурная кнопка
FILLEDTONALBUTTON() — кнопка с мягким фоном
TEXTBUTTON() — текстовая кнопка без фона
ICONBUTTON() — кнопка с иконкой

## Поля ввода

OUTLINEDTEXTFIELD() — поле ввода с контуром

## Контейнеры

COLUMN() — размещает элементы сверху вниз
ROW() — размещает элементы слева направо
BOX() — размещает элементы поверх друг друга
LAZYCOLUMN() — прокручиваемый список
SCAFFOLD() — каркас экрана с отступами
CARD() — карточка для группировки

## Диалоги

ALERTDIALOG() — всплывающее окно подтверждения
DATEPICKERDIALOG() — окно выбора даты
DATEPICKER() — календарь

## Переключатели

SWITCH() — переключатель true/false

---

# 13. Modifier (настройка элементов)

FILLMAXSIZE() — занять весь экран
FILLMAXWIDTH() — занять всю ширину
FILLMAXWIDTH(0.8f) — занять 80% ширины
PADDING(24.DP) — отступ со всех сторон
PADDING(TOP = 16.DP) — отступ только сверху
HEIGHT(200.DP) — высота элемента
WIDTH(12.DP) — ширина элемента
WEIGHT(1F) — распределение места в Row/Column
ALIGN(ALIGNMENT.CENTER) — выравнивание по центру
ALIGN(ALIGNMENT.BOTTOMCENTER) — выравнивание внизу по центру
ALIGN(ALIGNMENT.TOPCENTER) — выравнивание вверху по центру
NAVIGATIONBARSPADDING() — отступ от системной панели
IMEPADDING() — отступ от клавиатуры

---

# 14. Стили Material 3

## Типография

MATERIALTHEME.TYPOGRAPHY.DISPLAYMEDIUM — очень крупный текст
MATERIALTHEME.TYPOGRAPHY.HEADLINEMEDIUM — заголовок экрана
MATERIALTHEME.TYPOGRAPHY.HEADLINESMALL — заголовок поменьше
MATERIALTHEME.TYPOGRAPHY.TITLELARGE — подзаголовок крупный
MATERIALTHEME.TYPOGRAPHY.TITLEMEDIUM — подзаголовок средний
MATERIALTHEME.TYPOGRAPHY.TITLESMALL — подзаголовок мелкий
MATERIALTHEME.TYPOGRAPHY.BODYLARGE — основной текст крупный
MATERIALTHEME.TYPOGRAPHY.BODYMEDIUM — основной текст средний
MATERIALTHEME.TYPOGRAPHY.BODYSMALL — основной текст мелкий

## Цвета

MATERIALTHEME.COLORSCHEME.PRIMARY — основной цвет приложения
MATERIALTHEME.COLORSCHEME.ERROR — цвет ошибки (красный)
MATERIALTHEME.COLORSCHEME.SURFACEVARIANT — фон карточек
MATERIALTHEME.COLORSCHEME.PRIMARYCONTAINER — выделенный фон карточек
MATERIALTHEME.COLORSCHEME.ONSURFACEVARIANT — цвет подсказок

---

# 15. Работа с сетью (Retrofit)

RETROFITCLIENT — объект подключения к серверу
APISERVICE — интерфейс с методами API
ADDUSER(FNAME, LNAME, URL, NUM) — запрос регистрации пользователя
RESPONSE.ID — ID созданного пользователя

---

# 16. Логирование

LOG.D("TAG", "сообщение") — обычное сообщение в Logcat
LOG.E("TAG", "сообщение") — сообщение об ошибке в Logcat

---