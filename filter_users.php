<?php
header('Content-Type: application/json');

// Database connection parameters
$host = 'localhost';
$dbname = 'ami';
$username = 'root';
$password = '';

try {
    // Connect to the database
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Get interests and userId from POST request
    $input = json_decode(file_get_contents("php://input"), true);
    $interests = $input['interests'] ?? []; // Safely get interests
    $userId = $input['userId'] ?? null; // Safely get userId

    if (empty($interests) || empty($userId)) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Interests or userId not provided.'
        ]);
        exit;
    }

    // Create placeholders for SQL query
    $placeholders = implode(',', array_fill(0, count($interests), '?'));
    $query = "SELECT id, name, age, university, about, filter, photo 
              FROM profile_data 
              WHERE JSON_CONTAINS(filter, JSON_ARRAY($placeholders)) 
              AND id != ?";

    $params = array_merge($interests, [$userId]);
    
    // Debugging: Log the query and parameters
    error_log("Query: " . $query);
    error_log("Params: " . json_encode($params));
    
    $stmt = $pdo->prepare($query);
    $stmt->execute($params);
    
    // Fetch results and prepare response
    $results = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Convert photo blob to base64
    foreach ($results as &$result) {
        if ($result['photo']) {
            $result['photo'] = base64_encode($result['photo']);
        }
    }

    // Return response
    echo json_encode([
        'status' => 'success',
        'users' => $results
    ]);

} catch (PDOException $e) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Database connection error: ' . $e->getMessage()
    ]);
}
?>
