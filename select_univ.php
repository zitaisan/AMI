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

// Проверяем, что параметры "university" и "userId" переданы в запросе
if (!isset($_GET['university']) || empty($_GET['university']) || !isset($_GET['userId']) || empty($_GET['userId'])) {
    echo json_encode(['status' => 'error', 'message' => 'Не переданы параметры университета или ID пользователя']);
    exit();
}

$university = $_GET['university']; // Получаем университет из GET-запроса
$userId = $_GET['userId'];

// Подготовка и выполнение запроса на выборку данных из таблицы profile_data
$stmt = $pdo->prepare("SELECT name, photo FROM profile_data WHERE university = :university AND id != :id");
$stmt->execute([':university' => $university, ':id' => $userId]); // Используем университет и id из GET-запроса

// Получение всех результатов в виде ассоциативного массива
$users = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Проверяем, найдены ли пользователи
if ($users !== false && count($users) > 0) {
    // Преобразуем фото в base64
    foreach ($users as &$user) {
        $user['photo'] = base64_encode($user['photo']);
    }
    echo json_encode(['status' => 'success', 'users' => $users]);
} else {
    echo json_encode(['status' => 'error', 'message' => 'Пользователи не найдены']);
}
?>
