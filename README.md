# 🎟️ TixClick 

TixClick is a Java application designed for ticket sales and event creation, providing efficient ticket management and an optimized user experience. 🚀  

## 📥 Installation  

Use git [TixClick](https://github.com/SEP419-PSE/BE_PSE) to install the application.  

```bash
git clone https://github.com/SEP419-PSE/BE_PSE.git
cd BE_PSE
```

## 🚀 Usage

```python
mvn clean install
mvn spring-boot:run
```
The backend will start at 🌐 https://localhost:8443 by default.
## 🔗 API Example


You can test the API using tools like Postman or cURL.

🎫 Create an Event
```python
curl -X 'POST' \
  'https://localhost:8443/event/create?eventName=string&location=string&typeEvent=string&description=string&categoryId=0' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "logoURL": "string",
  "bannerURL": "string",
  "logoOrganizeURL": "string"
}'
```
## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss your proposal.

Make sure to update tests as appropriate before submitting changes.
## License

[MIT](https://choosealicense.com/licenses/mit/)
