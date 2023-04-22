package top.itning.yunshunas.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author itning
 * @since 2023/4/9 14:14
 */
public class ConfigChangeEvent extends ApplicationEvent {

    public ConfigChangeEvent(Object source) {
        super(source);
    }
}
