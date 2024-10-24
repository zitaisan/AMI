<?php
$host = 'localhost';
$dbname = 'ami'; // Замените на ваше имя базы данных
$username = 'root';
$password = '';

try {
    // Соединение с базой данных
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    echo json_encode(['status' => 'error', 'message' => 'Ошибка подключения к базе данных: ' . $e->getMessage()]);
    exit();
}

// Получаем id_user из GET-запроса
if (!isset($_GET['userId']) || empty($_GET['userId'])) {
    echo json_encode(['status' => 'error', 'message' => 'Не передан userId']);
    exit();
}

$userId = $_GET['userId'];

// Запрос для получения данных из таблицы chat_data и profile_data
$sql = "
    SELECT pd.name AS title, pd.photo AS photoBlob, cd.description_text AS description
    FROM chat_data cd
    JOIN profile_data pd ON cd.id = pd.id
    WHERE cd.id_user = :userId";

$stmt = $pdo->prepare($sql);
$stmt->execute([':userId' => $userId]);

$items = $stmt->fetchAll(PDO::FETCH_ASSOC);

if ($items) {
    // Преобразуем данные из поля photoBlob (LONG BLOB) в Base64
    foreach ($items as &$item) {
        if (!empty($item['photoBlob'])) {
            // Преобразование бинарных данных изображения в Base64
            $item['photoUrl'] = 'data:image/jpeg;base64,' . base64_encode($item['photoBlob']);
        } else {
            $item['photoUrl'] = null; // Если фото нет, возвращаем null
        }
        unset($item['photoBlob']); // Убираем поле с бинарными данными
    }
    
    echo json_encode(['status' => 'success', 'items' => $items]);
} else {
    echo json_encode(['status' => 'error', 'message' => 'Данные не найдены']);
}
?>
