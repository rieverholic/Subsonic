package dev.riever.subsonic.inject;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class WrappedChannelInitializer extends ChannelInitializer<Channel> {
    private static final Method INIT_CHANNEL;
    static {
        try {
            INIT_CHANNEL = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            INIT_CHANNEL.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final ChannelInitializer<?> delegate;
    private final Consumer<Channel> consumer;

    public WrappedChannelInitializer(ChannelInitializer<?> delegate, Consumer<Channel> consumer) {
        this.delegate = delegate;
        this.consumer = consumer;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        INIT_CHANNEL.invoke(this.delegate, channel);
        this.consumer.accept(channel);
    }
}
