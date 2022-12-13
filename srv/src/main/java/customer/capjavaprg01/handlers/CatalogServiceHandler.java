package customer.capjavaprg01.handlers;

import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.math.BigDecimal;
//import java.time.LocalDate;
import java.time.LocalDateTime;
//import java.time.LocalTime;
//import org.joda.time.LocalDateTime;


import com.sap.cds.services.cds.CdsService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.ServiceName;

import cds.gen.catalogservice.CatalogService_;
import cds.gen.catalogservice.Books;
import cds.gen.catalogservice.SaveMultipleBooksContext;
import cds.gen.catalogservice.BookTypes;
import cds.gen.catalogservice.POrders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.sap.cds.services.cds.CdsService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.core.CallableStatementCreator;
import cds.gen.catalogservice.*;

import com.sap.cds.Result;
import com.sap.cds.Struct;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.cqn.CqnAnalyzer;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.ql.cqn.CqnInsert;

import com.sap.cds.reflect.CdsModel;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.cds.CdsCreateEventContext;

import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.draft.DraftService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.messages.Messages;
import com.sap.cds.services.persistence.PersistenceService;
import static cds.gen.catalogservice.CatalogService_.BOOKS;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.datamodel.odata.client.exception.ODataException;
import com.sap.cloud.sdk.datamodel.odata.client.exception.ODataServiceErrorException;
import com.sap.cloud.sdk.datamodel.odata.client.exception.ODataServiceError;
import com.sap.cloud.sdk.datamodel.odata.client.exception.ODataDeserializationException;
import com.sap.cloud.sdk.datamodel.odata.client.exception.ODataResponseException;

import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultPurchaseOrderService;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.PurchaseOrderService;
//import com.sap.cloud.sdk.s4hana.datamodel.odata.services.PurchaseOrderItem;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.purchaseorder.PurchaseOrder;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.purchaseorder.PurchaseOrderCreateFluentHelper;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.purchaseorder.PurchaseOrderFluentHelper;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.purchaseorder.PurchaseOrderItem;

//import com.sap.cloud.sdk.datamodel.odatav4.core.SimpleProperty.DateTime;

@Component
@ServiceName(CatalogService_.CDS_NAME)
public class CatalogServiceHandler implements EventHandler {

    private List<POrders> capPOrderss = new ArrayList<>();
    private PurchaseOrderService purchaseOrderService;
    private static final long serialVersionUID = 1L;
    //private HttpDestination httpDestination = DestinationAccessor.getDestination("S4H").asHttp();        

    private static final Logger logger = LoggerFactory.getLogger(CatalogServiceHandler.class);	
    @Autowired
    private  PersistenceService db;

   // CatalogServiceHandler() {}


//    @On(entity = Books_.CDS_NAME)
    @On(event = CqnService.EVENT_READ, entity = POrders_.CDS_NAME) 
    public void onRead(CdsReadEventContext context) throws ODataException{
        //CdsModel model = context.getModel();
        Map<String, Object> v_row = new HashMap<>();
        Map<Object, Map<String, Object>> v_result = new HashMap<>();

        HttpDestination httpDestination = DestinationAccessor.getDestination("S4H_30").asHttp();  
        purchaseOrderService = new DefaultPurchaseOrderService();
        PurchaseOrderFluentHelper helper = purchaseOrderService.getAllPurchaseOrder().top(5);
        List<PurchaseOrder> purchaseOrders = helper.executeRequest(httpDestination);
        //logger.info("purchaseOrderItemTexts size:" + purchaseOrders.size());
        
        //StringBuffer stringBuffer = new StringBuffer();
        for (PurchaseOrder item: purchaseOrders) {
            //logger.info(item.toString());

            POrders capPOrders = com.sap.cds.Struct.create(POrders.class);

            capPOrders.setPoid(item.getPurchaseOrder());
            capPOrders.setPotype(item.getPurchaseOrderType());
            capPOrders.setPogroup(item.getPurchasingGroup());
            capPOrders.setPosupplier(item.getSupplier());
            
            capPOrderss.add(capPOrders);

            v_row.put("poid",item.getPurchaseOrder());
            v_row.put("potype",item.getPurchaseOrderType());
            v_row.put("pogroup",item.getPurchasingGroup());
            v_row.put("posupplier",item.getSupplier());

            v_result.put(item.getPurchaseOrder(), v_row);
        }

        //context.setResult(v_result.values());
        context.setResult(capPOrderss);
    }


    @On(event = CdsService.EVENT_CREATE, entity = POrders_.CDS_NAME)
    public void onCreate(CdsCreateEventContext context) throws ODataException {
        HttpDestination httpDestination = DestinationAccessor.getDestination("S4H_30").asHttp();            
        PurchaseOrderService service = new DefaultPurchaseOrderService();
        //LocalDateTime ldtime = new LocalDateTime().now();
        LocalDateTime currentDateTime = LocalDateTime.now();
        //BigDecimal qty = new BigDecimal(1);
        // qty = 1;

        Map<String, Object> m = context.getCqn().entries().get(0);
        BigDecimal qty2 = new BigDecimal(m.get("qty").toString());

        PurchaseOrderItem poitem = PurchaseOrderItem.builder().
                                    purchaseOrderItem("10".toString()).
                                    material("TG11".toString()).
                                    plant("1710".toString()).
                                    incotermsLocation1("VENDER".toString()).
                                    orderQuantity(qty2).build();
        
        PurchaseOrder po = PurchaseOrder.builder().purchasingGroup("001".toString()).
        companyCode("1710".toString()).
        //purchaseOrderType("NB".toString()).
        purchaseOrderType(m.get("potype").toString()).
        supplier("17300001".toString()).
        purchasingOrganization("1710".toString()).
        purchaseOrderItem(poitem).
        build();

        //BusinessPartner bp = BusinessPartner.builder().firstName(m.get("firstName").toString()).lastName(m.get("surname").toString()).businessPartner(m.get("ID").toString()).build();
        try {
            service.createPurchaseOrder(po).executeRequest(httpDestination);
        }
        catch( ODataServiceErrorException e) {
            ODataServiceError odataError = e.getOdataError();
            logger.debug("The OData service responded with an error: {}", odataError);

        } catch( ODataDeserializationException e ) {
            // handle failures in deserialization
            System.out.println("The OData Deserialization responded with an error: {}"+ e.getMessage());

        } catch( ODataResponseException e ) {
            // handle all other errors originating from handling the HTTP response
            int httpCode = e.getHttpCode();
            logger.debug("The OData Response1 responded with an error: {}", e.getHttpHeaders());
            logger.debug("The OData Response2 responded with an error: {}", e.getHttpBody());

        }

            capPOrderss.clear();
            POrders capPOrders = com.sap.cds.Struct.create(POrders.class);

            capPOrders.setPoid("NEW PO");
            capPOrders.setPotype("NB");
            capPOrders.setPogroup("001");
            capPOrders.setPosupplier("17300001");

            capPOrderss.add(capPOrders);

        context.setResult(capPOrderss);

    }    

    @On(event = SaveBookContext.CDS_NAME)
	public void onSaveBook(SaveBookContext context) {

        Map<String, Object> book = new HashMap<>();
        book.put("ID", 101);
        book.put("title", "Capire 2");
        book.put("stock", 100);
        CqnInsert insert = Insert.into("CatalogService.Books").entry(book);
        db.run(insert);
        context.setCompleted();

	}  

	@After(event = CdsService.EVENT_READ)
	public void discountBooks(Stream<Books> books) {
		books.filter(b -> b.getTitle() != null && b.getStock() != null)
		.filter(b -> b.getStock() > 200)
		.forEach(b -> b.setTitle(b.getTitle() + " (discounted)"));
	}


    //action SaveBookSingleTypesEntity (Books : BookTypes) returns BookTypes;

    @On(event = SaveSingleBookContext.CDS_NAME)
    public void onSaveBookTypesEntityProc(SaveSingleBookContext context) {

        BookTypes booktypes = BookTypes.create();
        Books books = Books.create();
        books.setId(context.getId());
        books.setTitle(context.getTitle());
        books.setStock(context.getStock());

        booktypes.setId(context.getId());
        booktypes.setTitle(context.getTitle());
        booktypes.setStock(context.getStock());
        
        Result res = db.run(Insert.into(BOOKS).entry(books));       
        cds.gen.catalogservice.Books inserted = res.single(cds.gen.catalogservice.Books.class);

        context.setResult(booktypes);

    }    

    @On(event = SaveMultipleBooksContext.CDS_NAME)
    public void onSaveBookTypesEntityProc(SaveMultipleBooksContext context) {

        Collection<BookTypes> bookTypes = context.getBooks();

        for (BookTypes bookType : bookTypes) {
            //bookType.getId();
            //bookType.getTitle();
            //bookType.getStock();
            Result res = db.run(Insert.into(BOOKS).entry(bookType));       
            cds.gen.catalogservice.Books inserted = res.single(cds.gen.catalogservice.Books.class);
     
        }
        context.setResult(bookTypes);

    }    


}