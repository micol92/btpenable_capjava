using my.bookshop as my from '../db/data-model';

service CatalogService @(requires: 'any') {
    entity Books as projection on my.Books ;
    entity POrders as projection on my.PurchaseOrders ;
    
    type BookTypes : {
        ID   : Integer;
        title  : String;
        stock  : Integer;
    }

    action SaveBook ();

    action SaveMultipleBooks (Books : array of BookTypes) returns array of BookTypes;

    action SaveSingleBook (ID : Integer, title : String, stock : Integer) returns BookTypes;

}
