<?php
session_start(); // Начинаем сессию

// Настройки подключения к базе данных
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "ami";

// Создаем соединение
$conn = new mysqli($servername, $username, $password, $dbname);

// Проверяем соединение
if ($conn->connect_error) {
    die(json_encode(array("status" => "error", "message" => "Ошибка подключения: " . $conn->connect_error)));
}

// Получаем данные из POST-запроса
$email = $_POST['mail'];
$password = $_POST['password'];
$studentNumber = $_POST['student_number'];

// Проверка, что все данные переданы
if (empty($email) || empty($password) || empty($studentNumber)) {
    echo json_encode(array("status" => "error", "message" => "Все поля обязательны для заполнения"));
    exit;
}

// Проверяем, существует ли пользователь с таким же email
$sql = "SELECT * FROM users_data WHERE mail = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    echo json_encode(array("status" => "error", "message" => "Пользователь с таким email уже существует"));
    exit;
}

// Хешируем пароль перед сохранением
$hashedPassword = password_hash($password, PASSWORD_DEFAULT);

// Запрос на вставку данных в таблицу
$sql = "INSERT INTO users_data (mail, password, student_number) VALUES (?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("sss", $email, $hashedPassword, $studentNumber);

if ($stmt->execute()) {
    // Получаем ID последней вставленной записи
    $userId = $stmt->insert_id; // Получаем id нового пользователя
    $_SESSION['user_id'] = $userId; // Сохраняем id в сессии
    error_log("Сессия сохранена: " . $_SESSION['user_id']); // Записываем в лог

    // Возвращаем ID пользователя вместе с сообщением
    echo json_encode(array(
        "status" => "success",
        "message" => "Регистрация прошла успешно",
        "user_id" => $userId // Добавляем user_id в ответ
    ));
} else {
    echo json_encode(array("status" => "error", "message" => "Ошибка при регистрации: " . $stmt->error));
}

// Закрываем соединение
$stmt->close();
$conn->close();
?>
