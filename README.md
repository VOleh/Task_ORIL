# Task_ORIL
Simple CRUD app with registration, login and logout forms

# Steps to reproduce:
- Download the current repository;
- Run your MongoDB server locally;
- Run the project;

# Description:
CRUD test application without any roles. All users have access only to LOGIN and REGISTRATION forms. 
The main fields are email and password, also we have validations for these fields.
All other endpoints are blocked. You should start your simple account (the registration form), after that you need to 
login, as a result, you'll get a cookie with JWT token. So the server has a filter which checks all your request using a token.
One user can create as many as possible new users, also he can find some user by id or get all users from DataBase. 
However,  user can update information only about yourself.
