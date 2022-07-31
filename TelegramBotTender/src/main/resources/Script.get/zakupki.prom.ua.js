//ПАРАМЕТРИ ЗАПИТ - Обов'язкові 
var keyword;
var init = function(keyword2) { keyword = keyword2; };
var query = 'https://zakupki.prom.ua/gov/tenders?q='+ keyword;

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
var pages = 5;

var searchTenders = function() {
    var list = new java.util.ArrayList();
    for (var i = 1; i <= pages; i++) {
//ЗАПИТ З ПЕРЕДАЧОМ ПАРАМЕТРУ З НАСТУПНОЮ СТОРІНКОЮ ТАБЛИЦІ ТЕНДЕРІВ

        query = 'https://zakupki.prom.ua/gov/tenders?q='+ encodeURI(keyword) + '&p=' + i;  
        doc = Connector.getDoc(query);

//КОЛЕКЦІЯ ЕЛЕМЕНТІВ ТЕНДЕРІВ
        var tenders = Selector.select(doc, 'div.zkb-list > div');

//ПАРСИНГ НЕОБХІДНИХ ПОЛІВ ТЕНДЕРА
        forEach.call(tenders, function(v) {
            var tender_id =  v.select('span.h-select-all').text();
            var url = decodeURI(v.select('a.zkb-list__heading.qa_title_link').attr('href').substr(0,300));
            var organization = v.select('div.zkb-list__about > bubble').attr('label');
            var subject = v.select('a.zkb-list__heading.qa_title_link').text().substr(0,1500);
            var start_date = v.select('div.zkb-list__timeline > div:nth-child(2) > p').text().substr(0,14);
            var end_date = v.select('div.zkb-list__timeline > div:nth-child(2) > p').text().substr(16,31);
            var price = v.select('p.zkb-list__price-value').text().substr(0,30);
            var tender_page = Connector.getDoc(url);
            var status = tender_page.select('zkb-numeric-list__cell qa_lot_status').text();

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
