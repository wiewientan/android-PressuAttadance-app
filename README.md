# PressuAttend – Android Attendance App

PressuAttend is an **Android attendance application** developed using **Android Studio** and **Java**, with **Firebase** as the backend database and authentication service.

This application supports **two roles: Teacher and Student**, each with different access and features.

## User Roles & Features

### Teacher
Teachers have full access to manage student data, including:
- Add new student data
- Update student information
- Delete student data
- Search students
- View student attendance records

### Student
Students have limited access, including:
- View their own profile details
- View their own attendance information

## Technologies Used
- Android Studio
- Java
- Firebase Authentication
- Firebase Realtime Database / Firestore

## Firebase Configuration
This project uses **Firebase**. To run the application properly, you must connect it to your own Firebase project.

Steps:
1. Create a project in **Firebase Console**
2. Add an Android app to the Firebase project
3. Download the `google-services.json` file
4. Place the file inside the `app/` directory
5. Enable Authentication and Database services in Firebase
6. Sync the project in Android Studio

## How to Run the Project
1. Open the project using **Android Studio**
2. Connect the project to Firebase
3. Let Gradle finish syncing
4. Run the app on an Android Emulator or physical device

## Notes
- This project is created for learning and educational purposes
- Firebase configuration is required before running the application
- Students can only access their own data, while teachers have full management access
