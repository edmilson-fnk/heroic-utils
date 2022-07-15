package heroic;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public class ExcelFile {

    private final String fileName;
    private final Workbook workbook;
    private Map<Long, Map<ServerVoiceChannel, Collection<User>>> counts;

    public ExcelFile(String fileName) throws IOException {
        this.fileName = fileName;
        this.workbook = new XSSFWorkbook();
    }

    public String generateWorkbook(Server server, Map<Long, Map<ServerVoiceChannel, Collection<User>>> counts) throws IOException {
        this.counts = counts;
        addResumeSheet();
        addDetailedSheets(server);
        return writeFile();
    }

    public void addResumeSheet() {
        Sheet summarySheet = this.workbook.createSheet("Resumo");
        Row header = summarySheet.createRow(0);
        header.createCell(0).setCellValue("Horário");
        header.createCell(1).setCellValue("Quantidade");

        int currentRow = 1;
        for (Long time : this.counts.keySet()) {
            String hour = Utils.convertMsToHour(time);
            Row dataRow = summarySheet.createRow(currentRow);
            long count = this.counts.get(time).values().stream().mapToLong(Collection::size).sum();

            dataRow.createCell(0).setCellValue(hour);
            dataRow.createCell(1).setCellValue((int) count);
            currentRow++;
        }
    }

    public void addDetailedSheets(Server server) {
        for (Long time : this.counts.keySet()) {
            Sheet timeSheet;
            String sheetName = Utils.convertMsToHourName(time);
            while (true) {
                try {
                    timeSheet =  this.workbook.createSheet(sheetName);
                    break;
                } catch (Exception e) {
                    sheetName = sheetName + "-2";
                }
            }
            Row header = timeSheet.createRow(0);
            header.createCell(0).setCellValue("Canal");
            header.createCell(1).setCellValue("Membro");

            int totalRow = 1;
            for (Map.Entry<ServerVoiceChannel, Collection<User>> entry : this.counts.get(time).entrySet()) {
                ArrayList<User> users = new ArrayList<>(entry.getValue());
                if (users.isEmpty() || (users.size() == 1 && users.get(0) == null)) {
                    continue;
                }
                Row userRow = timeSheet.createRow(totalRow);
                userRow.createCell(0).setCellValue(entry.getKey().getName());
                userRow.createCell(1).setCellValue(users.get(0).getNickname(server).orElse(users.get(0).getName()));
                totalRow++;
                for (User user : users.subList(1, users.size())) {
                    if (user == null) {
                        continue;
                    }
                    Row newUserRow = timeSheet.createRow(totalRow);
                    newUserRow.createCell(0).setCellValue("");
                    newUserRow.createCell(1).setCellValue(user.getNickname(server).orElse(user.getName()));
                    totalRow++;
                }
            }

        }
    }

    private String writeFile() throws IOException {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + this.fileName + ".xlsx";

        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        this.workbook.write(outputStream);
        this.workbook.close();

        return fileLocation;
    }

}
