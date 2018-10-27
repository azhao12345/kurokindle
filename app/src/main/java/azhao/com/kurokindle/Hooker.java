package azhao.com.kurokindle;
import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;

import java.lang.reflect.Method;
import java.util.List;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hooker implements IXposedHookLoadPackage {
    private static Tokenizer tokenizer = Tokenizer.builder().build();
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.amazon.kindle"))
            return;

        //XposedBridge.log("we are in the app!");

        findAndHookMethod("com.amazon.kcp.reader.ui.dictionary.BaseDictionaryDocument", lpparam.classLoader, "lookupDefinition", String.class, String.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                // this will be called before the clock was updated by the original method

                //XposedBridge.log("before!");
                //XposedBridge.log("first " + (String)param.args[0]);
                //XposedBridge.log("second " + (String)param.args[1]);

                // call the original thing
                Object dictionary = param.thisObject;
                //Method originalMethod = dictionary.getClass().getMethod("lookupDefinition",  String.class, String.class);
                Method originalMethod = (Method)param.method;

                Object originalDictionaryLookup = originalMethod.invoke(dictionary, param.args[0], param.args[1]);
                if (originalDictionaryLookup == null) {
                    XposedBridge.log("original lookup was null!");
                    List<Token> result = tokenizer.tokenize((String)param.args[0]);

                    //XposedBridge.log("found base form" + result.get(0).getBaseForm());
                    Object modifiedLookup = originalMethod.invoke(dictionary, result.get(0).getBaseForm(), param.args[1]);
                    return modifiedLookup;
                } else {
                    //XposedBridge.log("original lookup was not null! " + originalDictionaryLookup.toString());
                    return originalDictionaryLookup;
                }
            }
        });
    }
}
