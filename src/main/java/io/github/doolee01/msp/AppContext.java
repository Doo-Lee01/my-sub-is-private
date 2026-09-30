package io.github.doolee01.msp;

import io.github.doolee01.msp.repository.AccountRepository;
import io.github.doolee01.msp.repository.MessageRepository;
import io.github.doolee01.msp.repository.jdbc.Database;
import io.github.doolee01.msp.repository.jdbc.DatabaseConnectionException;
import io.github.doolee01.msp.repository.jdbc.JdbcAccountRepository;
import io.github.doolee01.msp.repository.jdbc.JdbcMessageRepository;
import io.github.doolee01.msp.repository.memory.MemoryAccountRepository;
import io.github.doolee01.msp.repository.memory.MemoryMessageRepository;
import io.github.doolee01.msp.service.DemoData;
import io.github.doolee01.msp.service.InstaService;

/**
 * 프로그램의 "조립 공장". 어떤 부품(저장소)을 쓸지 여기서 딱 한 번 정해요.
 *
 *  - 환경 변수 DB_URL이 있으면 → JDBC 저장소 (Supabase)
 *  - 없으면                    → 메모리 저장소 (DB 없이 바로 실행)
 *
 * InstaService 입장에서는 둘 다 그냥 AccountRepository라서 코드가 똑같아요.
 */
public final class AppContext {

    private final InstaService service;
    private final String storageDescription;

    private AppContext(InstaService service, String storageDescription) {
        this.service = service;
        this.storageDescription = storageDescription;
    }

    /**
     * throws DatabaseConnectionException: DB 모드인데 연결이 안 되면 여기서 바로 알려줘요.
     * checked 예외라서 이 메서드를 부르는 WebApp, ConsoleApp은 반드시 처리해야 해요.
     */
    public static AppContext create() throws DatabaseConnectionException {
        AccountRepository accounts;
        MessageRepository messages;
        String description;

        Database database = Database.fromEnvironment();
        if (database == null) {
            accounts = new MemoryAccountRepository();
            messages = new MemoryMessageRepository();
            description = "메모리 (껐다 켜면 초기화돼요)";
        } else {
            database.verifyConnection();   // 연결이 안 되면 여기서 예외가 던져지고 아래 줄은 실행되지 않아요
            accounts = new JdbcAccountRepository(database);
            messages = new JdbcMessageRepository(database);
            description = "PostgreSQL · " + database.describe();
        }

        InstaService service = new InstaService(accounts, messages);
        if (service.isEmpty()) {
            DemoData.seed(service);
        }
        return new AppContext(service, description);
    }

    public InstaService getService() {
        return service;
    }

    public String getStorageDescription() {
        return storageDescription;
    }
}
