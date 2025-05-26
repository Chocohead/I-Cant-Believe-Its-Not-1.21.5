package com.chocohead.icbin1215;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAtNode;
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableInjectNode;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;

public class FixBrokenTargets implements MixinAnnotationAdjuster {
	private static void insertArguments(String owner, MethodNode method, Type... extra) {
		assert extra.length > 0; //A waste of time otherwise
		Type[] args = Type.getArgumentTypes(method.desc);

		int split = -1, slot = Modifier.isStatic(method.access) ? 0 : 1;
		for (int i = 0; i < args.length; slot += args[i++].getSize()) {
			String arg = args[i].getInternalName();

			if ("org/spongepowered/asm/mixin/injection/callback/CallbackInfo".equals(arg) ||
					"org/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable".equals(arg)) {
				split = i;
				break;
			}
		}
		assert split >= 0: owner + '#' + method.name + method.desc;

		args = ArrayUtils.insert(split, args, extra);
		method.desc = Type.getMethodDescriptor(Type.getReturnType(method.desc), args);

		int shift = 0;
		for (Type arg : extra) {
			shift += arg.getSize();
		}

		for (AbstractInsnNode insn : method.instructions) {
			if (insn.getType() == AbstractInsnNode.VAR_INSN) {
				VarInsnNode vin = (VarInsnNode) insn;

				if (vin.var >= slot) vin.var += shift;
			}
		}
		for (LocalVariableNode var : method.localVariables) {
			if (var.index >= slot) var.index += shift;
		}

		ClassInfo info = ClassInfo.fromCache(owner);
		assert info != null: owner;
		try {
			MethodUtils.invokeMethod(info, true, "addMethod", method);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Error modifying ClassInfo for " + owner + '#' + method.name + method.desc, e.getCause());
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Error trying to modify ClassInfo for " + owner + '#' + method.name + method.desc, e);
		}
		assert info.findMethod(method, ClassInfo.INCLUDE_ALL) != null: owner + '#' + method.name + method.desc;
	}

	@Override
	public AdjustableAnnotationNode adjust(List<String> targetClasses, String mixinClass, MethodNode handler, AdjustableAnnotationNode annotation) {
		assert !"Lcom/llamalad7/mixinextras/sugar/impl/SugarWrapper;".equals(annotation.desc): mixinClass;

		switch (mixinClass) {
		case "fuzs.puzzleslib.fabric.mixin.client.ClientLevelFabricMixin":
		case "dev.architectury.mixin.fabric.client.MixinClientLevel": {
			if (!annotation.is(Inject.class)) break; //Some other kind of injector
			AdjustableInjectNode inject = annotation.as(AdjustableInjectNode.class);
			if (!inject.getMethod().contains("<init>")) break; //Some other injection

			Type listType = Type.getType(List.class);
			insertArguments(mixinClass, handler, listType, listType);

			inject.applyRefmap();
			annotation = inject.withMethod(methods -> {
				assert methods.size() == 1: methods;
				String method = methods.get(0);

				assert "Lnet/minecraft/class_638;<init>(Lnet/minecraft/class_634;Lnet/minecraft/class_638$class_5271;Lnet/minecraft/class_5321;Lnet/minecraft/class_6880;IILnet/minecraft/class_761;ZJI)V".equals(method): method;
				methods.set(0, method.replace("I)", "ILjava/util/List;Ljava/util/List;)"));

				return methods;
			});
			break;
		}
		case "fuzs.puzzleslib.fabric.mixin.ServerPlayerFabricMixin": {
			if (!annotation.is(Inject.class)) break; //Some other kind of injector
			AdjustableInjectNode inject = annotation.as(AdjustableInjectNode.class);
			if (!inject.getMethod().contains("drop(Z)Z")) break; //Some other injection

			annotation = inject.withAt(locations -> {
				assert locations.size() == 1: locations;
				AdjustableAtNode at = locations.get(0);

				at.applyRefmap();
				String target = at.getTarget();
				assert "Lnet/minecraft/class_3222;method_7329(Lnet/minecraft/class_1799;ZZ)Lnet/minecraft/class_1542;".equals(target): target;
				at.setTarget(target.replace("ZZ)", "ZZZ)"));

				return locations;
			});
			break;
		}
		case "dev.architectury.mixin.fabric.MixinPlayerList": {
			if (!annotation.is(Inject.class)) break; //Some other kind of injector
			AdjustableInjectNode inject = annotation.as(AdjustableInjectNode.class);
			if (!inject.getMethod().contains("respawn")) break; //Some other injection

			insertArguments(mixinClass, handler, Type.getType(Optional.class));

			inject.applyRefmap();
			annotation = inject.withMethod(methods -> {
				assert methods.size() == 1: methods;
				String method = methods.get(0);

				assert "Lnet/minecraft/class_3324;method_14556(Lnet/minecraft/class_3222;ZLnet/minecraft/class_1297$class_5529;)Lnet/minecraft/class_3222;".equals(method): method;
				methods.set(0, method.replace(";)", ";Ljava/util/Optional;)"));

				return methods;
			});
			break;
		}
		case "net.pitan76.mcpitanlib.mixin.MinecraftServerTimer": {
			if (!annotation.is(Inject.class)) break; //Some other kind of injector
			AdjustableInjectNode inject = annotation.as(AdjustableInjectNode.class);
			if (!inject.getMethod().contains("tick")) break; //Some other injection

			inject.applyRefmap();
			annotation = inject.withMethod(methods -> {
				assert methods.size() == 1: methods;
				String method = methods.get(0);

				assert "Lnet/minecraft/server/MinecraftServer;method_3748(Ljava/util/function/BooleanSupplier;)V".equals(method): method;
				methods.set(0, method.replace("(L", "(Lnet/minecraft/class_10961;L"));

				return methods;
			});
			break;
		}
		}

		return annotation;
	}
}