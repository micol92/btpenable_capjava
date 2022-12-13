namespace my.bookshop;

entity Books {
  key ID : Integer;
  title  : String;
  stock  : Integer;
}

entity PurchaseOrders {
  key poid : String;
  potype  : String;
  pogroup  : String;
  posupplier : String;
  qty : Integer;
}