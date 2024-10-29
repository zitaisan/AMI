<?php
session_start();

// Настройки подключения к базе данных
$host = 'localhost';
$dbname = 'ami';
$username = 'root'; // ваш пользователь БД, по умолчанию root
$password = '';     // ваш пароль к БД, по умолчанию пустой

// Соединение с базой данных
try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    // Возвращаем ошибку в формате JSON
    echo json_encode(['status' => 'error', 'message' => 'Ошибка подключения к базе данных: ' . $e->getMessage()]);
    exit();
}

$response = ['status' => 'error', 'message' => ''];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $mail = $_POST['mail'];
    $pass = $_POST['password'];
    $num = $_POST['student_number'];

    // Проверка введенных данных
    $stmt = $pdo->prepare("SELECT id, mail, password, student_number FROM users_data WHERE mail = :mail");
    $stmt->execute(['mail' => $mail]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    // Отладочная информация: выводим значения переменных
    if ($user) {
        file_put_contents('log.txt', "Почта: $mail = {$user['mail']} \n", FILE_APPEND);
        file_put_contents('log.txt', "Пароль: $pass = {$user['password']} \n", FILE_APPEND);
        file_put_contents('log.txt', "Num: $num = {$user['student_number']} \n", FILE_APPEND);
    } else {
        file_put_contents('log.txt', "Пользователь не найден для почты: $mail \n", FILE_APPEND);
    }

    if ($user) {
        // Проверяем правильность пароля
        if (password_verify($pass, $user['password']) && ($num == $user['student_number'])) {
            $_SESSION['user'] = $user['mail'];
            $_SESSION['student_number'] = $user['student_number'];
            $_SESSION['user_id'] = $user['id']; // Сохраняем ID пользователя в сессию

            // Возврат успешного ответа
            $response['status'] = 'success';
            $response['message'] = 'Авторизация успешна';
            $response['user'] = $user['mail'];
            $response['user_id'] = $user['id']; // Возвращаем user_id
            $response['student_number'] = $user['student_number']; // Возвращаем student_number
        } else {
            $response['message'] = 'Неверный e-mail или пароль!';
        }
    } else {
        $response['message'] = 'Пользователь не найден!';
    }
} else {
    $response['message'] = 'Неверный метод запроса!';
}

// Устанавливаем заголовок и выводим JSON-ответ
header('Content-Type: application/json');
echo json_encode($response);
?>
