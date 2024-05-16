package com.tencent.supersonic.headless.server.listener;


import com.tencent.supersonic.headless.core.chat.knowledge.DictWord;
import com.tencent.supersonic.headless.core.chat.knowledge.KnowledgeService;
import com.tencent.supersonic.headless.server.service.impl.WordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@Order(2)
public class ApplicationStartedListener implements CommandLineRunner {

    @Autowired
    private KnowledgeService knowledgeService;
    @Autowired
    private WordService wordService;

    @Override
    public void run(String... args) {
        updateKnowledgeDimValue();
    }

    public Boolean updateKnowledgeDimValue() {
        Boolean isOk = false;
        try {
            log.debug("ApplicationStartedInit start");

            List<DictWord> dictWords = wordService.getAllDictWords();
            wordService.setPreDictWords(dictWords);
            knowledgeService.reloadAllData(dictWords);

            log.debug("ApplicationStartedInit end");
            isOk = true;
        } catch (Exception e) {
            log.error("ApplicationStartedInit error", e);
        }
        return isOk;
    }

    public Boolean updateKnowledgeDimValueAsync() {
        CompletableFuture.supplyAsync(() -> {
            updateKnowledgeDimValue();
            return null;
        });
        return true;
    }

    /***
     * reload knowledge task
     */
    @Scheduled(cron = "${reload.knowledge.corn:0 0/1 * * * ?}")
    public void reloadKnowledge() {
        log.debug("reloadKnowledge start");

        try {
            List<DictWord> dictWords = wordService.getAllDictWords();
            List<DictWord> preDictWords = wordService.getPreDictWords();

            if (CollectionUtils.isEqualCollection(dictWords, preDictWords)) {
                log.debug("dictWords has not changed, reloadKnowledge end");
                return;
            }
            log.info("dictWords has changed");
            wordService.setPreDictWords(dictWords);
            knowledgeService.updateOnlineKnowledge(wordService.getAllDictWords());
        } catch (Exception e) {
            log.error("reloadKnowledge error", e);
        }

        log.debug("reloadKnowledge end");
    }
}
