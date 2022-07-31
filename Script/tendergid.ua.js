//ПАРАМЕТРИ ЗАПИТ - Обов'язкові 
var keyword;
var init = function(keyword2) { keyword = keyword2; };
var query = 'https://tendergid.ua/ua/тендери/tenderinfo/'+ keyword + '/sort/published:desc/type/1.html';

//ПАРАМЕТРИ ЗАПИТ - Опціональні
var recordsPerPage = 10;

//ІНІЦІАЛІЗАЦІЯ КОМПОНЕНТІВ JAVA - Обов'язково
var Connector = Java.type('org.kramadi.bot.look4tender.Connector');
var Selector = Java.type('org.kramadi.bot.look4tender.Selector');
// var DB = Java.type('tender_flow.DataBase');

//ІНІЦІАЛІЗАЦІЯ КОМПОНЕНТІВ JAVA - Для роботи циклу у скрипті
var forEach = Array.prototype.forEach;

//ОТРИМАННЯ .html ДОКУМЕНТА ЗА ПЕРЕДАНИМ ЗАПИТОМ - Обов'язково

var doc = Connector.getDoc(query);

//ЗМІННІ ДЛЯ РОБОТИ ЦИКЛУ ПОСТОРІНКОВОГО ПАРСИНГУ - Опціонально
var pages = 1;

var searchTenders = function() {
    var list = new java.util.ArrayList();
    for (var i = 0; i <= pages; i++) {
//ЗАПИТ З ПЕРЕДАЧОМ ПАРАМЕТРУ З НАСТУПНОЮ СТОРІНКОЮ ТАБЛИЦІ ТЕНДЕРІВ
        var resultI; 
        if(i == 0){
            resultI = 0;
        }else if(i == 1){
            resultI = 25;
        }
        query = 'https://tendergid.ua/ua/тендери/tenderinfo/'+ encodeURI(keyword) + '/sort/published:desc/type/1/page/' + resultI;
        doc = Connector.getDoc(query);

//КОЛЕКЦІЯ ЕЛЕМЕНТІВ ТЕНДЕРІВ
        var tenders = Selector.select(doc, 'tbody > tr');

//ПАРСИНГ НЕОБХІДНИХ ПОЛІВ ТЕНДЕРА
        forEach.call(tenders, function(v) {
            var tender_id =  v.select('td:nth-child(1) > div').attr('id');
            var url = decodeURI(v.select('td:nth-child(5) > a').attr('href').substr(2,300));
            var organization = v.select('td:nth-child(4)').text().substr(0,499);
            var subject = v.select('td:nth-child(3)').text();
            var start_date = v.select('td:nth-child(2)').text().substr(0,11);
            var end_date = v.select('td:nth-child(5)').text().substr(0,11);
            var status = v.select('td:nth-child(6)').text();
            var price = v.select('td:nth-child(7)').text();

            list.add(new Tender(tender_id, subject, status, price, organization, url, start_date, end_date));
        });
    }

    return list;
};

//КЛАС ТЕНДЕРА - Для зручності обов'язковий
function Tender(id, subject, status, price, organization, url ,start_date, end_date) {
    this.id = id;
    this.subject = subject;
    this.status = status;
    this.price = price;
    this.organization = organization;
    this.url = url;
    this.start_date = start_date;
    this.end_date = end_date;
}
