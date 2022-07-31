//ПАРАМЕТРИ ЗАПИТ - Обов'язкові 
var keyword;
var init = function(keyword2) { keyword = keyword2; };
var query = 'https://smarttender.biz/publichni-zakupivli-prozorro/?q='+ keyword + '&s=7';

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

        query = 'https://tender.uub.com.ua/PositionList.aspx?&page='+ i +'&filter_type=filter&fvFTWord_0='+ encodeURI(keyword) +'&action=Y';  
        doc = Connector.getDoc(query);

//КОЛЕКЦІЯ ЕЛЕМЕНТІВ ТЕНДЕРІВ
        var tenders = Selector.select(doc, '#pnList > div');

//ПАРСИНГ НЕОБХІДНИХ ПОЛІВ ТЕНДЕРА
        forEach.call(tenders, function(v) {
            var tender_id =  v.select('div.col-12.mb-1.text-primary').text();
            var url = 'https://tender.uub.com.ua/' + decodeURI(v.select('.mb-1.h2 > a').attr('href').substr(0,300));
            var organization = v.select('div.col-12.col-md > div > div:nth-child(2) > a').text();
            var subject = v.select('div.col-12.mb-1.h2 > a').text().substr(0,1500);
            //var start_date = v.select('div.padding-top-15.trade-number > span').text();
            //var end_date = v.select('div > div:nth-child(2) > div.trade-status-date').text();
            var price = v.select('div.col-12.font-weight-bold.text-success > span').text();
            var status = v.select('div.text-info.lang').text();
            var tender_page = Connector.getDoc(url);
            var start_date = tender_page.select('#tPosition_last_Modified').text();
            var end_date = tender_page.select('#tPosition_tenderPeriod_endDate').text();

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
