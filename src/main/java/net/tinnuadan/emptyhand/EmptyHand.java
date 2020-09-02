package net.tinnuadan.emptyhand;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;


public class EmptyHand implements ModInitializer {
  private KeyBinding _keyBinding;
  private static final MinecraftClient _mc = MinecraftClient.getInstance();
  public static final Identifier EMPTY_HAND = new Identifier("net.tinnuadan.emptyhand", "emptyhand");

  @Override
  public void onInitialize() {
    System.out.println("Initializing EmptyHand Mod");

    _keyBinding = new KeyBinding("key.empty_hand.empty_hand", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_0, "category.empty_hand.empty_hand");

    KeyBindingHelper.registerKeyBinding(_keyBinding);


    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      while (_keyBinding.wasPressed()) {
        _mc.player.inventory.selectedSlot = 9;
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeInt(8369);
        // Send packet to server to change the block for us
        ClientSidePacketRegistry.INSTANCE.sendToServer(EmptyHand.EMPTY_HAND, passedData);
      }
    });

    ServerSidePacketRegistry.INSTANCE.register(EMPTY_HAND, (packetContext, attachedData) -> {
      int payload = attachedData.readInt();
      packetContext.getTaskQueue().execute(() -> {
        // Execute on the main thread
        // ALWAYS validate that the information received is valid in a C2S packet!
        if(payload == 8369)
        {
          // Will trigger "<Player> tried to set an invalid carried item" in the server logs
          packetContext.getPlayer().inventory.selectedSlot = 9;
        }
      });
    });


    System.out.println("EmptyHand Mod initialized");
  }
}
