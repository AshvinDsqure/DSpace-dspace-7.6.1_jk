package org.dspace.app.itemimport;

import org.apache.commons.cli.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.RelationshipType;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.RelationshipService;
import org.dspace.content.service.RelationshipTypeService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;

import java.sql.SQLException;
import java.util.UUID;

public class ConnectedCase {
    private  static  final ItemService itemService= ContentServiceFactory.getInstance().getItemService();
    protected static final RelationshipTypeService relationshipTypeService=ContentServiceFactory.getInstance().getRelationshipTypeService();
    protected static final RelationshipService relationshipTypeservice=ContentServiceFactory.getInstance().getRelationshipService();
    private static final EPersonService epersonService = EPersonServiceFactory.getInstance().getEPersonService();
    public static void main(String[] args) {
        try {
            CommandLineParser parser = new DefaultParser();
            Options options = new Options();
            options.addOption("e", "eperson", true, "email of eperson doing importing");
            options.addOption("m", "MainCase", true, "MainCase");
            options.addOption("c", "ConnectedCase", true, "CConnectedCase");
            CommandLine line = parser.parse(options, args);
            String mainCase = ""; // Replace with your actual PDF file path
            String connectedCase="";
            String eperson="";
            if (!line.hasOption('e')) {
                System.out.println("Error  with, eperson  must add");
                System.exit(1);
            }
            if (!line.hasOption('m')) {
                System.out.println("Error  with, Main Case");
                System.exit(1);
            }
            if (!line.hasOption('c')) {
                System.out.println("Error  with, Conneced Case");
                System.exit(1);
            }
            if (line.hasOption('e')) { // eperson
                eperson = line.getOptionValue('t');
            }
            if (line.hasOption('m')) { // eperson
                mainCase = line.getOptionValue('m');
            }
            if (line.hasOption('c')) { // eperson
                connectedCase = line.getOptionValue('c');
            }
            System.out.println("Main Case::"+mainCase);
            System.out.println("Connected Case::"+connectedCase);
            Context context = new Context();
            EPerson ePerson = epersonService.findByEmail(context, eperson);
            context.setCurrentUser(ePerson);
            context.turnOffAuthorisationSystem();
            RelationshipType relationshipType = relationshipTypeService.find(context, 53);
            Item leftItem = itemService.find(context, UUID.fromString(mainCase));

            Item rightItem = itemService.find(context, UUID.fromString(connectedCase));
            if (leftItem != null && rightItem != null) {
                relationshipTypeservice.create(context, leftItem, rightItem, relationshipType, 0, 0);
            }
            context.complete();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (AuthorizeException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
