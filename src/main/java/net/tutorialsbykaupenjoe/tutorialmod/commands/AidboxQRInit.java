package net.tutorialsbykaupenjoe.tutorialmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.arguments.EntityArgument;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;


public class AidboxQRInit {
    public AidboxQRInit(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("aidboxinit")
                        .then(Commands.argument("arg1", StringArgumentType.word())
                                .then(Commands.argument("arg2", StringArgumentType.word())
                                        .executes(context -> initAidboxQR(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "arg1"),
                                                StringArgumentType.getString(context, "arg2"))))));
    }

    private int initAidboxQR(CommandSource source, String arg1, String arg2) 
    throws CommandSyntaxException {
        ServerPlayerEntity player = source.asPlayer();
        String playerId = player.getGameProfile().getId().toString();
        String playerName = player.getGameProfile().getName();
        sendPlayerDataToServer(source, playerId, playerName, arg1);

        source.sendFeedback(new StringTextComponent("Player ID: " + playerId), true);
        source.sendFeedback(new StringTextComponent("Player Name: " + playerName), true);
        source.sendFeedback(new StringTextComponent("Command: /aidboxinit " + arg1 + " " + arg2), true);
        
        return 1;
    }

    private void sendPlayerDataToServer(CommandSource source, String playerId, String playerName, String arg1) {
        try {
            //Auth Bullshit:

            String username = "go-cath-lab";
            String password = "gE4RwD"; 
            String authString = username + ":" + password;
            String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes());
            String authHeaderValue = "Basic " + encodedAuthString;

            URL patientURL = new URL("http://localhost:8282/Patient"); 
            URL questRespURL = new URL("http://localhost:8282/QuestionnaireResponse");    // Replace with your server endpoint
            HttpURLConnection patientConnection = (HttpURLConnection) patientURL.openConnection();
            HttpURLConnection questUrlConnection = (HttpURLConnection) questRespURL.openConnection();

            patientConnection.setRequestProperty("Authorization", authHeaderValue);
            questUrlConnection.setRequestProperty("Authorization", authHeaderValue);
            
            patientConnection.setRequestMethod("PUT");
            patientConnection.setRequestProperty("Content-Type", "application/json");
            patientConnection.setDoOutput(true);

            questUrlConnection.setRequestMethod("PUT");
            questUrlConnection.setRequestProperty("Content-Type", "application/json");
            questUrlConnection.setDoOutput(true);
            
            String patientRespFmt = "{\"id\":\"%s\",\"name\":[{\"use\":\"nickname\",\"text\":\"%s\"}]}";
            String questRespFmt = "{\"status\":\"completed\",\"subject\":{\"resourceType\":\"Patient\",\"id\":\"%s\"},\"authored\":\"2023-12-28\",\"item\":[{\"linkId\":\"1\",\"answer\":[{\"value\":{\"string\":\"%s\"}}]}]}";
            
            String patientJsonPayload = String.format(patientRespFmt,playerId,playerName);
            String questRespJsonPayload = String.format(questRespFmt,playerId, arg1);

           try (OutputStream os = patientConnection.getOutputStream()) {
                byte[] input = patientJsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (OutputStream os = questUrlConnection.getOutputStream()) {
                byte[] input = questRespJsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int patientResponseCode = patientConnection.getResponseCode();
            int questResponseCode = questUrlConnection.getResponseCode(); 

            patientConnection.disconnect();
            questUrlConnection.disconnect();

            String result = "HTTP Response Codes: " + patientResponseCode + questResponseCode; 

            source.sendFeedback(new StringTextComponent(result), true);

            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
