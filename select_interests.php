<?php
$host = 'localhost';
$dbname = 'ami';
$username = 'root';
$password = '';
header('Content-Type: application/json');


try {
    // Подключаемся к базе данных
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Подготовка и выполнение запроса на выборку всех интересов
    $stmt = $pdo->prepare("SELECT name FROM interests");
    $stmt->execute();

    // Получение всех результатов в виде ассоциативного массива
    $interests = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Проверяем, найдены ли интересы
    if ($interests !== false && count($interests) > 0) {
        echo json_encode(['status' => 'success', 'interests' => $interests]);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Интересы не найдены']);
    }
} catch (PDOException $e) {
    echo json_encode(['status' => 'error', 'message' => 'Ошибка подключения к базе данных: ' . $e->getMessage()]);
}
?>
