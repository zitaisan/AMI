<?php
// Настройки подключения к базе данных
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

// Получение данных
$userId = $_POST['user_id']; // Получаем user_id из POST-запроса
$university = $_POST['university'];
$name = $_POST['name'];
$age = $_POST['age'];
$about = $_POST['about'];
$interestsJson = $_POST['interests']; // JSON с интересами

// Проверка загрузки фото
if (empty($_FILES['photo']['tmp_name'])) {
    echo json_encode(['status' => 'error', 'message' => 'Файл фото не загружен.']);
    exit();
}

$photo = $_FILES['photo']['tmp_name'];
$photoData = file_get_contents($photo);

// Вставка данных в таблицу profile_data
$stmt = $pdo->prepare("INSERT INTO profile_data (id, name, age, photo, about, filter, university) VALUES (:id, :name, :age, :photo, :about, :interestsJson, :university)");
$stmt->execute([
    ':id' => $userId, // Используем id пользователя из POST-запроса
    ':name' => $name,
    ':age' => $age,
    ':photo' => $photoData,
    ':about' => $about,
    ':interestsJson' => $interestsJson,
    ':university' => $university,
]);

// Успешный ответ
echo json_encode(['status' => 'success', 'message' => 'Данные успешно добавлены.']);
?>
