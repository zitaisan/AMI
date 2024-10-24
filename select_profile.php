<?php
$host = 'localhost';
$dbname = 'ami';
$username = 'root';
$password = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    echo json_encode(['status' => 'error', 'message' => 'Ошибка подключения к базе данных: ' . $e->getMessage()]);
    exit();
}

// Проверяем, что параметр "id" передан в запросе
if (!isset($_GET['id']) || empty($_GET['id'])) {
    echo json_encode(['status' => 'error', 'message' => 'Не передан параметр ID']);
    exit();
}

$id = $_GET['id']; // Получаем ID пользователя из GET-запроса

// Подготовка и выполнение запроса на выборку данных из таблицы profile_data
$stmt = $pdo->prepare("SELECT id, name, photo, about FROM profile_data WHERE id = :id");
$stmt->execute([':id' => $id]); // Используем ID из GET-запроса

// Получение результата в виде ассоциативного массива
$user = $stmt->fetch(PDO::FETCH_ASSOC);

// Проверяем, найден ли пользователь
if ($user !== false) {
    // Преобразуем фото в base64
    $user['photo'] = base64_encode($user['photo']);
    echo json_encode(['status' => 'success', 'user' => $user]);
} else {
    echo json_encode(['status' => 'error', 'message' => 'Пользователь не найден']);
}
?>
