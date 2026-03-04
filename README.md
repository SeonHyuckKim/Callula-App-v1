# callula-server


1.시작 하려면
docker run --name callula-db-mysql -e MYSQL_ROOT_PASSWORD=1234 -p 3306:3306 -d mysql:8.0.32
요거로 db세팅후

오른쪽 database 화면에서 +버튼 눌러서 mysql이라는것을 찾아서 db 추가 후
@localhost -> local로 변경
user = root
password = 1234로 해서 시작


*Swagger Login
ID : admin
PASSWORD: 1234
