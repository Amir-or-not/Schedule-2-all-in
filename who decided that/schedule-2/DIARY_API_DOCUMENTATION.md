# API Документация - Электронный Дневник

## Роут для генерации аналитики через FastAPI

**POST** `/api/analytics/generate`

**Request Body:**
```json
{
  "prompt": "Проанализируй последние новости"
}
```

**Response:**
```json
{
  "id": 1,
  "prompt": "Проанализируй последние новости",
  "answer": "Анализ новостей...",
  "metricName": "Проанализируй последние новости",
  "metricValue": null,
  "timestamp": "2025-11-06T00:00:00Z"
}
```

---

## Роуты Электронного Дневника

Все роуты начинаются с `/api/diary`

### 📊 GRADES (Оценки)

#### Получить все оценки
**GET** `/api/diary/grades`

#### Получить оценку по ID
**GET** `/api/diary/grades/{id}`

#### Получить оценки студента
**GET** `/api/diary/grades/student/{studentId}`

#### Получить оценки группы
**GET** `/api/diary/grades/group/{groupId}`

#### Получить оценки студента по предмету
**GET** `/api/diary/grades/student/{studentId}/subject/{subject}`

#### Получить оценки по расписанию (все оценки по конкретному уроку)
**GET** `/api/diary/grades/schedule/{scheduleId}`

#### Получить оценки студента по конкретному уроку в расписании
**GET** `/api/diary/grades/student/{studentId}/schedule/{scheduleId}`

#### Получить полную статистику оценок студента (по всем предметам)
**GET** `/api/diary/grades/student/{studentId}/stats`

**Response:**
```json
{
  "studentId": "student123",
  "studentName": "Иван Иванов",
  "overallAverage": 4.5,
  "totalGrades": 15,
  "subjectStats": [
    {
      "subject": "Математика",
      "averageGrade": 4.7,
      "gradeCount": 5,
      "minGrade": 4,
      "maxGrade": 5,
      "grades": [
        {
          "id": 1,
          "studentId": "student123",
          "subject": "Математика",
          "gradeValue": 5,
          "gradeType": "exam",
          "lessonDate": "2025-11-06T10:00:00"
        }
      ]
    }
  ]
}
```

#### Получить средний балл студента по всем предметам
**GET** `/api/diary/grades/student/{studentId}/average`

**Response:**
```json
{
  "studentId": "student123",
  "averageGrade": 4.5
}
```

#### Получить средний балл студента по конкретному предмету
**GET** `/api/diary/grades/student/{studentId}/subject/{subject}/average`

**Response:**
```json
{
  "studentId": "student123",
  "subject": "Математика",
  "averageGrade": 4.7
}
```

#### Создать оценку
**POST** `/api/diary/grades`

**Request Body:**
```json
{
  "studentId": "student123",
  "subject": "Математика",
  "gradeValue": 5,
  "gradeType": "exam",
  "teacherId": "teacher456",
  "lessonDate": "2025-11-06T10:00:00",
  "comment": "Отлично выполнил работу",
  "groupId": "group-1",
  "scheduleId": "schedule-123"
}
```

**Примечание:** Если указан `scheduleId`, то `subject` и `groupId` автоматически заполняются из расписания, если они не указаны.

#### Обновить оценку
**PUT** `/api/diary/grades/{id}`

#### Удалить оценку
**DELETE** `/api/diary/grades/{id}`

---

### 📝 HOMEWORK (Домашние задания)

#### Получить все домашние задания
**GET** `/api/diary/homework`

#### Получить домашнее задание по ID
**GET** `/api/diary/homework/{id}`

#### Получить домашние задания группы
**GET** `/api/diary/homework/group/{groupId}`

#### Получить домашние задания студента
**GET** `/api/diary/homework/student/{studentId}`

#### Получить предстоящие домашние задания группы
**GET** `/api/diary/homework/group/{groupId}/upcoming`

#### Создать домашнее задание
**POST** `/api/diary/homework`

**Request Body:**
```json
{
  "title": "Решить задачи по алгебре",
  "description": "Страницы 45-50, задачи 1-10",
  "subject": "Математика",
  "teacherId": "teacher456",
  "groupId": "group-1",
  "dueDate": "2025-11-10T23:59:59",
  "assignedDate": "2025-11-06T10:00:00",
  "isCompleted": false,
  "studentId": null,
  "attachmentUrl": "https://example.com/file.pdf"
}
```

#### Обновить домашнее задание
**PUT** `/api/diary/homework/{id}`

#### Удалить домашнее задание
**DELETE** `/api/diary/homework/{id}`

---

### ✅ ATTENDANCE (Посещаемость)

#### Получить всю посещаемость
**GET** `/api/diary/attendance`

#### Получить посещаемость по ID
**GET** `/api/diary/attendance/{id}`

#### Получить посещаемость студента
**GET** `/api/diary/attendance/student/{studentId}`

#### Получить посещаемость группы
**GET** `/api/diary/attendance/group/{groupId}`

#### Получить посещаемость по дате
**GET** `/api/diary/attendance/date/{date}`

**Пример:** `/api/diary/attendance/date/2025-11-06`

#### Создать запись посещаемости
**POST** `/api/diary/attendance`

**Request Body:**
```json
{
  "studentId": "student123",
  "subject": "Математика",
  "attendanceDate": "2025-11-06",
  "status": "present",
  "teacherId": "teacher456",
  "groupId": "group-1",
  "comment": "Присутствовал на уроке"
}
```

**Статусы:**
- `"present"` - Присутствовал
- `"absent"` - Отсутствовал
- `"late"` - Опоздал
- `"excused"` - Уважительная причина

#### Обновить посещаемость
**PUT** `/api/diary/attendance/{id}`

#### Удалить посещаемость
**DELETE** `/api/diary/attendance/{id}`

---

## Примеры использования в Postman

### 1. Создать оценку (привязанную к расписанию)
```
POST http://localhost:8080/api/diary/grades
Content-Type: application/json

{
  "studentId": "student123",
  "scheduleId": "schedule-123",
  "gradeValue": 5,
  "gradeType": "exam"
}
```

**Примечание:** При указании `scheduleId` предмет и группа автоматически заполняются из расписания.

### 1.1. Создать оценку (без привязки к расписанию)
```
POST http://localhost:8080/api/diary/grades
Content-Type: application/json

{
  "studentId": "student123",
  "subject": "Математика",
  "gradeValue": 5,
  "gradeType": "exam",
  "groupId": "group-1"
}
```

### 2. Получить оценки студента
```
GET http://localhost:8080/api/diary/grades/student/student123
```

### 2.1. Получить оценки по расписанию
```
GET http://localhost:8080/api/diary/grades/schedule/schedule-123
```

### 2.2. Получить оценки студента по конкретному уроку
```
GET http://localhost:8080/api/diary/grades/student/student123/schedule/schedule-123
```

### 3. Создать домашнее задание
```
POST http://localhost:8080/api/diary/homework
Content-Type: application/json

{
  "title": "Домашнее задание по физике",
  "description": "Решить задачи 1-5",
  "subject": "Физика",
  "groupId": "group-1",
  "dueDate": "2025-11-10T23:59:59"
}
```

### 4. Отметить посещаемость
```
POST http://localhost:8080/api/diary/attendance
Content-Type: application/json

{
  "studentId": "student123",
  "subject": "Математика",
  "attendanceDate": "2025-11-06",
  "status": "present",
  "groupId": "group-1"
}
```

### 5. Генерация аналитики через FastAPI
```
POST http://localhost:8080/api/analytics/generate
Content-Type: application/json

{
  "prompt": "Проанализируй успеваемость студентов"
}
```

