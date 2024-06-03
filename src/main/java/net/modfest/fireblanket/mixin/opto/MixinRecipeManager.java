package net.modfest.fireblanket.mixin.opto;

import java.util.List;
import java.util.Optional;

import net.minecraft.recipe.RecipeEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

@Mixin(RecipeManager.class)
public class MixinRecipeManager {
// implemented by vanilla instead

//	@Inject(at=@At("HEAD"), method={
//			"getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/recipe/input/RecipeInput;Lnet/minecraft/world/World;)Ljava/util/Optional;"
//	}, cancellable = true)
//	public void fireblanket$getFirstMatch$DontScanEmptyGrids(RecipeType<T> type, I input, World world, CallbackInfoReturnable<Optional<RecipeEntry<T>>> cir) {
//
//	}
//
//	@Inject(at=@At("HEAD"), method={
//			"getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;"
//		}, cancellable=true)
//	public void fireblanket$getFirstMatch$DontScanEmptyGrids(RecipeType<?> type, Inventory inv, World world, CallbackInfoReturnable<Optional<?>> ci) {
//		if (type == RecipeType.CRAFTING && inv != null && inv.isEmpty()) {
//			ci.setReturnValue(Optional.empty());
//		}
//	}
//
//	@Inject(at=@At("HEAD"), method={
//			"getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;Lnet/minecraft/util/Identifier;)Ljava/util/Optional;",
//		}, cancellable=true)
//	public void fireblanket$getFirstMatch$DontScanEmptyGrids(RecipeType<?> type, Inventory inv, World world, Identifier id, CallbackInfoReturnable<Optional<?>> ci) {
//		if (type == RecipeType.CRAFTING && inv != null && inv.isEmpty()) {
//			ci.setReturnValue(Optional.empty());
//		}
//	}
//
//	@Inject(at=@At("HEAD"), method={
//			"getAllMatches(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/List;",
//		}, cancellable=true)
//	public void fireblanket$getAllMatches$DontScanEmptyGrids(RecipeType<?> type, Inventory inv, World world, CallbackInfoReturnable<List<?>> ci) {
//		if (type == RecipeType.CRAFTING && inv != null && inv.isEmpty()) {
//			ci.setReturnValue(List.of());
//		}
//	}
//
//	@Inject(at=@At("HEAD"), method={
//			"getRemainingStacks(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Lnet/minecraft/util/collection/DefaultedList;"
//		}, cancellable=true)
//	public void fireblanket$getRemainingStacks$DontScanEmptyGrids(RecipeType<?> type, Inventory inv, World world, CallbackInfoReturnable<DefaultedList<?>> ci) {
//		if (type == RecipeType.CRAFTING && inv != null && inv.isEmpty()) {
//			ci.setReturnValue(DefaultedList.of());
//		}
//	}
	
}
