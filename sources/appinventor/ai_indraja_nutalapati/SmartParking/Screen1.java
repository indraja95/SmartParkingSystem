package appinventor.ai_indraja_nutalapati.SmartParking;

import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AppInventorCompatActivity;
import com.google.appinventor.components.runtime.Button;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.FirebaseDB;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.HandlesEventDispatching;
import com.google.appinventor.components.runtime.Label;
import com.google.appinventor.components.runtime.Notifier;
import com.google.appinventor.components.runtime.PasswordTextBox;
import com.google.appinventor.components.runtime.TextBox;
import com.google.appinventor.components.runtime.VerticalScrollArrangement;
import com.google.appinventor.components.runtime.errors.PermissionException;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.RetValManager;
import com.google.appinventor.components.runtime.util.RuntimeErrorAlert;
import com.google.youngandroid.runtime;
import gnu.expr.Language;
import gnu.expr.ModuleBody;
import gnu.expr.ModuleInfo;
import gnu.expr.ModuleMethod;
import gnu.kawa.functions.Apply;
import gnu.kawa.functions.Format;
import gnu.kawa.functions.GetNamedPart;
import gnu.kawa.functions.IsEqual;
import gnu.kawa.reflect.Invoke;
import gnu.kawa.reflect.SlotGet;
import gnu.kawa.reflect.SlotSet;
import gnu.lists.Consumer;
import gnu.lists.FString;
import gnu.lists.LList;
import gnu.lists.Pair;
import gnu.lists.PairWithPosition;
import gnu.lists.VoidConsumer;
import gnu.mapping.CallContext;
import gnu.mapping.Environment;
import gnu.mapping.Procedure;
import gnu.mapping.SimpleSymbol;
import gnu.mapping.Symbol;
import gnu.mapping.Values;
import gnu.mapping.WrongType;
import gnu.math.IntNum;
import kawa.lang.Promise;
import kawa.lib.lists;
import kawa.lib.misc;
import kawa.lib.strings;
import kawa.standard.Scheme;

/* compiled from: Screen1.yail */
public class Screen1 extends Form implements Runnable {
    static final SimpleSymbol Lit0 = ((SimpleSymbol) new SimpleSymbol("Screen1").readResolve());
    static final SimpleSymbol Lit1 = ((SimpleSymbol) new SimpleSymbol("getMessage").readResolve());
    static final PairWithPosition Lit10 = PairWithPosition.make(Lit41, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45306);
    static final PairWithPosition Lit100 = PairWithPosition.make(Lit138, PairWithPosition.make((SimpleSymbol) new SimpleSymbol("list").readResolve(), LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 606330), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 606325);
    static final PairWithPosition Lit101 = PairWithPosition.make(Lit138, PairWithPosition.make(Lit138, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 606453), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 606448);
    static final PairWithPosition Lit102 = PairWithPosition.make(Lit19, PairWithPosition.make(Lit19, PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 606605), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 606600), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 606594);
    static final PairWithPosition Lit103 = PairWithPosition.make(Lit138, PairWithPosition.make(Lit138, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 606710), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 606705);
    static final PairWithPosition Lit104 = PairWithPosition.make(Lit138, PairWithPosition.make(Lit138, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 606868), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 606863);
    static final SimpleSymbol Lit105 = ((SimpleSymbol) new SimpleSymbol("StoreValue").readResolve());
    static final PairWithPosition Lit106 = PairWithPosition.make(Lit19, PairWithPosition.make(Lit138, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 607084), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 607078);
    static final PairWithPosition Lit107 = PairWithPosition.make(Lit19, PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 607272), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 607266);
    static final PairWithPosition Lit108 = PairWithPosition.make(Lit19, PairWithPosition.make(Lit19, PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 607333), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 607328), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 607322);
    static final PairWithPosition Lit109 = PairWithPosition.make(Lit138, PairWithPosition.make(Lit138, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 607438), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 607433);
    static final SimpleSymbol Lit11 = ((SimpleSymbol) new SimpleSymbol("pass").readResolve());
    static final SimpleSymbol Lit110 = ((SimpleSymbol) new SimpleSymbol("FirebaseDB1$TagList").readResolve());
    static final SimpleSymbol Lit111 = ((SimpleSymbol) new SimpleSymbol("TagList").readResolve());
    static final SimpleSymbol Lit112 = ((SimpleSymbol) new SimpleSymbol("$tag").readResolve());
    static final PairWithPosition Lit113 = PairWithPosition.make(Lit138, PairWithPosition.make(Lit138, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 614514), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 614509);
    static final PairWithPosition Lit114 = PairWithPosition.make(Lit138, PairWithPosition.make(Lit138, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 614648), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 614643);
    static final PairWithPosition Lit115 = PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 614765);
    static final PairWithPosition Lit116 = PairWithPosition.make(Lit138, PairWithPosition.make(Lit138, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 614893), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 614888);
    static final PairWithPosition Lit117 = PairWithPosition.make(Lit19, PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 615093), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 615087);
    static final PairWithPosition Lit118 = PairWithPosition.make(Lit19, PairWithPosition.make(Lit19, PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 615150), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 615145), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 615139);
    static final PairWithPosition Lit119;
    static final PairWithPosition Lit12 = PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45457);
    static final SimpleSymbol Lit120 = ((SimpleSymbol) new SimpleSymbol("FirebaseDB1$GotValue").readResolve());
    static final SimpleSymbol Lit121 = ((SimpleSymbol) new SimpleSymbol("GotValue").readResolve());
    static final FString Lit122 = new FString("com.google.appinventor.components.runtime.Notifier");
    static final FString Lit123 = new FString("com.google.appinventor.components.runtime.Notifier");
    static final SimpleSymbol Lit124 = ((SimpleSymbol) new SimpleSymbol("get-simple-name").readResolve());
    static final SimpleSymbol Lit125 = ((SimpleSymbol) new SimpleSymbol("android-log-form").readResolve());
    static final SimpleSymbol Lit126 = ((SimpleSymbol) new SimpleSymbol("add-to-form-environment").readResolve());
    static final SimpleSymbol Lit127 = ((SimpleSymbol) new SimpleSymbol("lookup-in-form-environment").readResolve());
    static final SimpleSymbol Lit128 = ((SimpleSymbol) new SimpleSymbol("is-bound-in-form-environment").readResolve());
    static final SimpleSymbol Lit129 = ((SimpleSymbol) new SimpleSymbol("add-to-global-var-environment").readResolve());
    static final PairWithPosition Lit13 = PairWithPosition.make(Lit41, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45484);
    static final SimpleSymbol Lit130 = ((SimpleSymbol) new SimpleSymbol("add-to-events").readResolve());
    static final SimpleSymbol Lit131 = ((SimpleSymbol) new SimpleSymbol("add-to-components").readResolve());
    static final SimpleSymbol Lit132 = ((SimpleSymbol) new SimpleSymbol("add-to-global-vars").readResolve());
    static final SimpleSymbol Lit133 = ((SimpleSymbol) new SimpleSymbol("add-to-form-do-after-creation").readResolve());
    static final SimpleSymbol Lit134 = ((SimpleSymbol) new SimpleSymbol("send-error").readResolve());
    static final SimpleSymbol Lit135 = ((SimpleSymbol) new SimpleSymbol("dispatchEvent").readResolve());
    static final SimpleSymbol Lit136 = ((SimpleSymbol) new SimpleSymbol("dispatchGenericEvent").readResolve());
    static final SimpleSymbol Lit137 = ((SimpleSymbol) new SimpleSymbol("lookup-handler").readResolve());
    static final SimpleSymbol Lit138 = ((SimpleSymbol) new SimpleSymbol("any").readResolve());
    static final PairWithPosition Lit14 = PairWithPosition.make(Lit138, PairWithPosition.make(Lit138, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45508), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45503);
    static final SimpleSymbol Lit15 = ((SimpleSymbol) new SimpleSymbol("$calledBy").readResolve());
    static final PairWithPosition Lit16 = PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45686);
    static final SimpleSymbol Lit17 = ((SimpleSymbol) new SimpleSymbol("FirebaseDB1").readResolve());
    static final SimpleSymbol Lit18 = ((SimpleSymbol) new SimpleSymbol("ProjectBucket").readResolve());
    static final SimpleSymbol Lit19;
    static final SimpleSymbol Lit2 = ((SimpleSymbol) new SimpleSymbol("*the-null-value*").readResolve());
    static final SimpleSymbol Lit20 = ((SimpleSymbol) new SimpleSymbol("GetTagList").readResolve());
    static final SimpleSymbol Lit21 = ((SimpleSymbol) new SimpleSymbol("Notifier1").readResolve());
    static final SimpleSymbol Lit22 = ((SimpleSymbol) new SimpleSymbol("ShowMessageDialog").readResolve());
    static final PairWithPosition Lit23 = PairWithPosition.make(Lit19, PairWithPosition.make(Lit19, PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 46051), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 46046), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 46040);
    static final PairWithPosition Lit24 = PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45279);
    static final PairWithPosition Lit25 = PairWithPosition.make(Lit41, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45306);
    static final PairWithPosition Lit26 = PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45457);
    static final PairWithPosition Lit27 = PairWithPosition.make(Lit41, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45484);
    static final PairWithPosition Lit28 = PairWithPosition.make(Lit138, PairWithPosition.make(Lit138, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45508), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45503);
    static final PairWithPosition Lit29 = PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45686);
    static final SimpleSymbol Lit3 = ((SimpleSymbol) new SimpleSymbol("g$state").readResolve());
    static final PairWithPosition Lit30 = PairWithPosition.make(Lit19, PairWithPosition.make(Lit19, PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 46051), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 46046), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 46040);
    static final SimpleSymbol Lit31 = ((SimpleSymbol) new SimpleSymbol("p$authenticateUser").readResolve());
    static final SimpleSymbol Lit32 = ((SimpleSymbol) new SimpleSymbol("GetValue").readResolve());
    static final PairWithPosition Lit33 = PairWithPosition.make(Lit19, PairWithPosition.make(Lit138, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 49351), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 49345);
    static final PairWithPosition Lit34 = PairWithPosition.make(Lit19, PairWithPosition.make(Lit138, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 49351), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 49345);
    static final SimpleSymbol Lit35 = ((SimpleSymbol) new SimpleSymbol("p$displayData").readResolve());
    static final SimpleSymbol Lit36 = ((SimpleSymbol) new SimpleSymbol("AlignHorizontal").readResolve());
    static final IntNum Lit37 = IntNum.make(3);
    static final SimpleSymbol Lit38 = ((SimpleSymbol) new SimpleSymbol("number").readResolve());
    static final SimpleSymbol Lit39 = ((SimpleSymbol) new SimpleSymbol("AppName").readResolve());
    static final SimpleSymbol Lit4 = ((SimpleSymbol) new SimpleSymbol("g$currentuser").readResolve());
    static final SimpleSymbol Lit40 = ((SimpleSymbol) new SimpleSymbol("Scrollable").readResolve());
    static final SimpleSymbol Lit41 = ((SimpleSymbol) new SimpleSymbol(PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN).readResolve());
    static final SimpleSymbol Lit42 = ((SimpleSymbol) new SimpleSymbol("ShowListsAsJson").readResolve());
    static final SimpleSymbol Lit43 = ((SimpleSymbol) new SimpleSymbol("Sizing").readResolve());
    static final SimpleSymbol Lit44 = ((SimpleSymbol) new SimpleSymbol("Title").readResolve());
    static final SimpleSymbol Lit45 = ((SimpleSymbol) new SimpleSymbol("TitleVisible").readResolve());
    static final FString Lit46 = new FString("com.google.appinventor.components.runtime.VerticalScrollArrangement");
    static final SimpleSymbol Lit47 = ((SimpleSymbol) new SimpleSymbol("Login_Block").readResolve());
    static final SimpleSymbol Lit48 = ((SimpleSymbol) new SimpleSymbol("Width").readResolve());
    static final IntNum Lit49 = IntNum.make(-1080);
    static final SimpleSymbol Lit5 = ((SimpleSymbol) new SimpleSymbol("g$currentpassword").readResolve());
    static final FString Lit50 = new FString("com.google.appinventor.components.runtime.VerticalScrollArrangement");
    static final FString Lit51 = new FString("com.google.appinventor.components.runtime.Label");
    static final SimpleSymbol Lit52 = ((SimpleSymbol) new SimpleSymbol("Signup").readResolve());
    static final SimpleSymbol Lit53 = ((SimpleSymbol) new SimpleSymbol("BackgroundColor").readResolve());
    static final IntNum Lit54;
    static final SimpleSymbol Lit55 = ((SimpleSymbol) new SimpleSymbol("FontBold").readResolve());
    static final SimpleSymbol Lit56 = ((SimpleSymbol) new SimpleSymbol("FontSize").readResolve());
    static final IntNum Lit57 = IntNum.make(24);
    static final SimpleSymbol Lit58 = ((SimpleSymbol) new SimpleSymbol("TextAlignment").readResolve());
    static final IntNum Lit59 = IntNum.make(1);
    static final SimpleSymbol Lit6 = ((SimpleSymbol) new SimpleSymbol("p$verifyUser").readResolve());
    static final SimpleSymbol Lit60 = ((SimpleSymbol) new SimpleSymbol("TextColor").readResolve());
    static final IntNum Lit61;
    static final IntNum Lit62 = IntNum.make(-2);
    static final FString Lit63 = new FString("com.google.appinventor.components.runtime.Label");
    static final FString Lit64 = new FString("com.google.appinventor.components.runtime.Label");
    static final SimpleSymbol Lit65 = ((SimpleSymbol) new SimpleSymbol("username").readResolve());
    static final FString Lit66 = new FString("com.google.appinventor.components.runtime.Label");
    static final FString Lit67 = new FString("com.google.appinventor.components.runtime.TextBox");
    static final SimpleSymbol Lit68 = ((SimpleSymbol) new SimpleSymbol("Height").readResolve());
    static final IntNum Lit69 = IntNum.make(40);
    static final SimpleSymbol Lit7 = ((SimpleSymbol) new SimpleSymbol("user").readResolve());
    static final FString Lit70 = new FString("com.google.appinventor.components.runtime.TextBox");
    static final FString Lit71 = new FString("com.google.appinventor.components.runtime.Label");
    static final SimpleSymbol Lit72 = ((SimpleSymbol) new SimpleSymbol("password").readResolve());
    static final FString Lit73 = new FString("com.google.appinventor.components.runtime.Label");
    static final FString Lit74 = new FString("com.google.appinventor.components.runtime.PasswordTextBox");
    static final FString Lit75 = new FString("com.google.appinventor.components.runtime.PasswordTextBox");
    static final FString Lit76 = new FString("com.google.appinventor.components.runtime.Button");
    static final SimpleSymbol Lit77 = ((SimpleSymbol) new SimpleSymbol("login").readResolve());
    static final IntNum Lit78;
    static final IntNum Lit79 = IntNum.make(16);
    static final SimpleSymbol Lit8 = ((SimpleSymbol) new SimpleSymbol("Text").readResolve());
    static final IntNum Lit80;
    static final FString Lit81 = new FString("com.google.appinventor.components.runtime.Button");
    static final SimpleSymbol Lit82 = ((SimpleSymbol) new SimpleSymbol("login$Click").readResolve());
    static final SimpleSymbol Lit83 = ((SimpleSymbol) new SimpleSymbol("Click").readResolve());
    static final FString Lit84 = new FString("com.google.appinventor.components.runtime.Label");
    static final SimpleSymbol Lit85 = ((SimpleSymbol) new SimpleSymbol("new_User").readResolve());
    static final FString Lit86 = new FString("com.google.appinventor.components.runtime.Label");
    static final FString Lit87 = new FString("com.google.appinventor.components.runtime.Button");
    static final SimpleSymbol Lit88 = ((SimpleSymbol) new SimpleSymbol("register").readResolve());
    static final IntNum Lit89;
    static final PairWithPosition Lit9 = PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 45279);
    static final IntNum Lit90;
    static final FString Lit91 = new FString("com.google.appinventor.components.runtime.Button");
    static final SimpleSymbol Lit92 = ((SimpleSymbol) new SimpleSymbol("register$Click").readResolve());
    static final FString Lit93 = new FString("com.google.appinventor.components.runtime.FirebaseDB");
    static final SimpleSymbol Lit94 = ((SimpleSymbol) new SimpleSymbol("DefaultURL").readResolve());
    static final SimpleSymbol Lit95 = ((SimpleSymbol) new SimpleSymbol("DeveloperBucket").readResolve());
    static final SimpleSymbol Lit96 = ((SimpleSymbol) new SimpleSymbol("FirebaseToken").readResolve());
    static final SimpleSymbol Lit97 = ((SimpleSymbol) new SimpleSymbol("FirebaseURL").readResolve());
    static final FString Lit98 = new FString("com.google.appinventor.components.runtime.FirebaseDB");
    static final SimpleSymbol Lit99 = ((SimpleSymbol) new SimpleSymbol("$value").readResolve());
    public static Screen1 Screen1;
    static final ModuleMethod lambda$Fn1 = null;
    static final ModuleMethod lambda$Fn10 = null;
    static final ModuleMethod lambda$Fn11 = null;
    static final ModuleMethod lambda$Fn12 = null;
    static final ModuleMethod lambda$Fn13 = null;
    static final ModuleMethod lambda$Fn14 = null;
    static final ModuleMethod lambda$Fn15 = null;
    static final ModuleMethod lambda$Fn16 = null;
    static final ModuleMethod lambda$Fn17 = null;
    static final ModuleMethod lambda$Fn18 = null;
    static final ModuleMethod lambda$Fn19 = null;
    static final ModuleMethod lambda$Fn2 = null;
    static final ModuleMethod lambda$Fn20 = null;
    static final ModuleMethod lambda$Fn21 = null;
    static final ModuleMethod lambda$Fn22 = null;
    static final ModuleMethod lambda$Fn23 = null;
    static final ModuleMethod lambda$Fn24 = null;
    static final ModuleMethod lambda$Fn25 = null;
    static final ModuleMethod lambda$Fn26 = null;
    static final ModuleMethod lambda$Fn27 = null;
    static final ModuleMethod lambda$Fn28 = null;
    static final ModuleMethod lambda$Fn29 = null;
    static final ModuleMethod lambda$Fn3 = null;
    static final ModuleMethod lambda$Fn30 = null;
    static final ModuleMethod lambda$Fn31 = null;
    static final ModuleMethod lambda$Fn32 = null;
    static final ModuleMethod lambda$Fn33 = null;
    static final ModuleMethod lambda$Fn34 = null;
    static final ModuleMethod lambda$Fn4 = null;
    static final ModuleMethod lambda$Fn5 = null;
    static final ModuleMethod lambda$Fn6 = null;
    static final ModuleMethod lambda$Fn7 = null;
    static final ModuleMethod lambda$Fn8 = null;
    static final ModuleMethod lambda$Fn9 = null;
    public Boolean $Stdebug$Mnform$St;
    public final ModuleMethod $define;
    public FirebaseDB FirebaseDB1;
    public final ModuleMethod FirebaseDB1$GotValue;
    public final ModuleMethod FirebaseDB1$TagList;
    public VerticalScrollArrangement Login_Block;
    public Notifier Notifier1;
    public Label Signup;
    public final ModuleMethod add$Mnto$Mncomponents;
    public final ModuleMethod add$Mnto$Mnevents;
    public final ModuleMethod add$Mnto$Mnform$Mndo$Mnafter$Mncreation;
    public final ModuleMethod add$Mnto$Mnform$Mnenvironment;
    public final ModuleMethod add$Mnto$Mnglobal$Mnvar$Mnenvironment;
    public final ModuleMethod add$Mnto$Mnglobal$Mnvars;
    public final ModuleMethod android$Mnlog$Mnform;
    public LList components$Mnto$Mncreate;
    public final ModuleMethod dispatchEvent;
    public final ModuleMethod dispatchGenericEvent;
    public LList events$Mnto$Mnregister;
    public LList form$Mndo$Mnafter$Mncreation;
    public Environment form$Mnenvironment;
    public Symbol form$Mnname$Mnsymbol;
    public final ModuleMethod get$Mnsimple$Mnname;
    public Environment global$Mnvar$Mnenvironment;
    public LList global$Mnvars$Mnto$Mncreate;
    public final ModuleMethod is$Mnbound$Mnin$Mnform$Mnenvironment;
    public Button login;
    public final ModuleMethod login$Click;
    public final ModuleMethod lookup$Mnhandler;
    public final ModuleMethod lookup$Mnin$Mnform$Mnenvironment;
    public Label new_User;
    public final ModuleMethod onCreate;
    public PasswordTextBox pass;
    public Label password;
    public final ModuleMethod process$Mnexception;
    public Button register;
    public final ModuleMethod register$Click;
    public final ModuleMethod send$Mnerror;
    public TextBox user;
    public Label username;

    /* compiled from: Screen1.yail */
    public class frame extends ModuleBody {
        Screen1 $main = this;

        public Object apply1(ModuleMethod moduleMethod, Object obj) {
            switch (moduleMethod.selector) {
                case 1:
                    return this.$main.getSimpleName(obj);
                case 2:
                    try {
                        this.$main.onCreate((Bundle) obj);
                        return Values.empty;
                    } catch (ClassCastException e) {
                        throw new WrongType(e, "onCreate", 1, obj);
                    }
                case 3:
                    this.$main.androidLogForm(obj);
                    return Values.empty;
                case 5:
                    try {
                        return this.$main.lookupInFormEnvironment((Symbol) obj);
                    } catch (ClassCastException e2) {
                        throw new WrongType(e2, "lookup-in-form-environment", 1, obj);
                    }
                case 7:
                    try {
                        return this.$main.isBoundInFormEnvironment((Symbol) obj) ? Boolean.TRUE : Boolean.FALSE;
                    } catch (ClassCastException e3) {
                        throw new WrongType(e3, "is-bound-in-form-environment", 1, obj);
                    }
                case 12:
                    this.$main.addToFormDoAfterCreation(obj);
                    return Values.empty;
                case 13:
                    this.$main.sendError(obj);
                    return Values.empty;
                case 14:
                    this.$main.processException(obj);
                    return Values.empty;
                case 23:
                    return Screen1.lambda6(obj);
                case 24:
                    return Screen1.lambda8(obj);
                case 55:
                    return this.$main.FirebaseDB1$TagList(obj);
                default:
                    return super.apply1(moduleMethod, obj);
            }
        }

        public Object apply0(ModuleMethod moduleMethod) {
            switch (moduleMethod.selector) {
                case 18:
                    return Screen1.lambda2();
                case 19:
                    this.$main.$define();
                    return Values.empty;
                case 20:
                    return Screen1.lambda3();
                case 21:
                    return Screen1.lambda4();
                case 22:
                    return Screen1.lambda5();
                case 25:
                    return Screen1.lambda7();
                case 26:
                    return Screen1.lambda9();
                case 27:
                    return Screen1.lambda11();
                case 28:
                    return Screen1.lambda10();
                case 29:
                    return Screen1.lambda12();
                case 30:
                    return Screen1.lambda14();
                case 31:
                    return Screen1.lambda13();
                case 32:
                    return Screen1.lambda15();
                case 33:
                    return Screen1.lambda16();
                case 34:
                    return Screen1.lambda17();
                case 35:
                    return Screen1.lambda18();
                case 36:
                    return Screen1.lambda19();
                case 37:
                    return Screen1.lambda20();
                case 38:
                    return Screen1.lambda21();
                case 39:
                    return Screen1.lambda22();
                case 40:
                    return Screen1.lambda23();
                case 41:
                    return Screen1.lambda24();
                case 42:
                    return Screen1.lambda25();
                case 43:
                    return Screen1.lambda26();
                case 44:
                    return Screen1.lambda27();
                case 45:
                    return Screen1.lambda28();
                case 46:
                    return Screen1.lambda29();
                case 47:
                    return this.$main.login$Click();
                case 48:
                    return Screen1.lambda30();
                case 49:
                    return Screen1.lambda31();
                case 50:
                    return Screen1.lambda32();
                case 51:
                    return Screen1.lambda33();
                case 52:
                    return this.$main.register$Click();
                case 53:
                    return Screen1.lambda34();
                case 54:
                    return Screen1.lambda35();
                default:
                    return super.apply0(moduleMethod);
            }
        }

        public int match0(ModuleMethod moduleMethod, CallContext callContext) {
            switch (moduleMethod.selector) {
                case 18:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 19:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 20:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 21:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 22:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 25:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 26:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 27:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 28:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 29:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 30:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 31:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 32:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 33:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 34:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 35:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 36:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 37:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 38:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 39:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 40:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 41:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 42:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 43:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 44:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 45:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 46:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 47:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 48:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 49:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 50:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 51:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 52:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 53:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                case 54:
                    callContext.proc = moduleMethod;
                    callContext.pc = 0;
                    return 0;
                default:
                    return super.match0(moduleMethod, callContext);
            }
        }

        public int match1(ModuleMethod moduleMethod, Object obj, CallContext callContext) {
            switch (moduleMethod.selector) {
                case 1:
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.pc = 1;
                    return 0;
                case 2:
                    if (!(obj instanceof Screen1)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.pc = 1;
                    return 0;
                case 3:
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.pc = 1;
                    return 0;
                case 5:
                    if (!(obj instanceof Symbol)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.pc = 1;
                    return 0;
                case 7:
                    if (!(obj instanceof Symbol)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.pc = 1;
                    return 0;
                case 12:
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.pc = 1;
                    return 0;
                case 13:
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.pc = 1;
                    return 0;
                case 14:
                    if (!(obj instanceof Screen1)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.pc = 1;
                    return 0;
                case 23:
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.pc = 1;
                    return 0;
                case 24:
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.pc = 1;
                    return 0;
                case 55:
                    callContext.value1 = obj;
                    callContext.proc = moduleMethod;
                    callContext.pc = 1;
                    return 0;
                default:
                    return super.match1(moduleMethod, obj, callContext);
            }
        }

        public int match2(ModuleMethod moduleMethod, Object obj, Object obj2, CallContext callContext) {
            switch (moduleMethod.selector) {
                case 4:
                    if (!(obj instanceof Symbol)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.proc = moduleMethod;
                    callContext.pc = 2;
                    return 0;
                case 5:
                    if (!(obj instanceof Symbol)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.proc = moduleMethod;
                    callContext.pc = 2;
                    return 0;
                case 8:
                    if (!(obj instanceof Symbol)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.proc = moduleMethod;
                    callContext.pc = 2;
                    return 0;
                case 9:
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.proc = moduleMethod;
                    callContext.pc = 2;
                    return 0;
                case 11:
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.proc = moduleMethod;
                    callContext.pc = 2;
                    return 0;
                case 17:
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.proc = moduleMethod;
                    callContext.pc = 2;
                    return 0;
                case 56:
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.proc = moduleMethod;
                    callContext.pc = 2;
                    return 0;
                default:
                    return super.match2(moduleMethod, obj, obj2, callContext);
            }
        }

        public int match4(ModuleMethod moduleMethod, Object obj, Object obj2, Object obj3, Object obj4, CallContext callContext) {
            switch (moduleMethod.selector) {
                case 10:
                    callContext.value1 = obj;
                    callContext.value2 = obj2;
                    callContext.value3 = obj3;
                    callContext.value4 = obj4;
                    callContext.proc = moduleMethod;
                    callContext.pc = 4;
                    return 0;
                case 15:
                    if (!(obj instanceof Screen1)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    if (!(obj2 instanceof Component)) {
                        return -786430;
                    }
                    callContext.value2 = obj2;
                    if (!(obj3 instanceof String)) {
                        return -786429;
                    }
                    callContext.value3 = obj3;
                    if (!(obj4 instanceof String)) {
                        return -786428;
                    }
                    callContext.value4 = obj4;
                    callContext.proc = moduleMethod;
                    callContext.pc = 4;
                    return 0;
                case 16:
                    if (!(obj instanceof Screen1)) {
                        return -786431;
                    }
                    callContext.value1 = obj;
                    if (!(obj2 instanceof Component)) {
                        return -786430;
                    }
                    callContext.value2 = obj2;
                    if (!(obj3 instanceof String)) {
                        return -786429;
                    }
                    callContext.value3 = obj3;
                    callContext.value4 = obj4;
                    callContext.proc = moduleMethod;
                    callContext.pc = 4;
                    return 0;
                default:
                    return super.match4(moduleMethod, obj, obj2, obj3, obj4, callContext);
            }
        }

        public Object apply4(ModuleMethod moduleMethod, Object obj, Object obj2, Object obj3, Object obj4) {
            boolean z = true;
            switch (moduleMethod.selector) {
                case 10:
                    this.$main.addToComponents(obj, obj2, obj3, obj4);
                    return Values.empty;
                case 15:
                    try {
                        try {
                            try {
                                try {
                                    return this.$main.dispatchEvent((Component) obj, (String) obj2, (String) obj3, (Object[]) obj4) ? Boolean.TRUE : Boolean.FALSE;
                                } catch (ClassCastException e) {
                                    throw new WrongType(e, "dispatchEvent", 4, obj4);
                                }
                            } catch (ClassCastException e2) {
                                throw new WrongType(e2, "dispatchEvent", 3, obj3);
                            }
                        } catch (ClassCastException e3) {
                            throw new WrongType(e3, "dispatchEvent", 2, obj2);
                        }
                    } catch (ClassCastException e4) {
                        throw new WrongType(e4, "dispatchEvent", 1, obj);
                    }
                case 16:
                    Screen1 screen1 = this.$main;
                    try {
                        Component component = (Component) obj;
                        try {
                            String str = (String) obj2;
                            try {
                                if (obj3 == Boolean.FALSE) {
                                    z = false;
                                }
                                try {
                                    screen1.dispatchGenericEvent(component, str, z, (Object[]) obj4);
                                    return Values.empty;
                                } catch (ClassCastException e5) {
                                    throw new WrongType(e5, "dispatchGenericEvent", 4, obj4);
                                }
                            } catch (ClassCastException e6) {
                                throw new WrongType(e6, "dispatchGenericEvent", 3, obj3);
                            }
                        } catch (ClassCastException e7) {
                            throw new WrongType(e7, "dispatchGenericEvent", 2, obj2);
                        }
                    } catch (ClassCastException e8) {
                        throw new WrongType(e8, "dispatchGenericEvent", 1, obj);
                    }
                default:
                    return super.apply4(moduleMethod, obj, obj2, obj3, obj4);
            }
        }

        public Object apply2(ModuleMethod moduleMethod, Object obj, Object obj2) {
            switch (moduleMethod.selector) {
                case 4:
                    try {
                        this.$main.addToFormEnvironment((Symbol) obj, obj2);
                        return Values.empty;
                    } catch (ClassCastException e) {
                        throw new WrongType(e, "add-to-form-environment", 1, obj);
                    }
                case 5:
                    try {
                        return this.$main.lookupInFormEnvironment((Symbol) obj, obj2);
                    } catch (ClassCastException e2) {
                        throw new WrongType(e2, "lookup-in-form-environment", 1, obj);
                    }
                case 8:
                    try {
                        this.$main.addToGlobalVarEnvironment((Symbol) obj, obj2);
                        return Values.empty;
                    } catch (ClassCastException e3) {
                        throw new WrongType(e3, "add-to-global-var-environment", 1, obj);
                    }
                case 9:
                    this.$main.addToEvents(obj, obj2);
                    return Values.empty;
                case 11:
                    this.$main.addToGlobalVars(obj, obj2);
                    return Values.empty;
                case 17:
                    return this.$main.lookupHandler(obj, obj2);
                case 56:
                    return this.$main.FirebaseDB1$GotValue(obj, obj2);
                default:
                    return super.apply2(moduleMethod, obj, obj2);
            }
        }
    }

    static {
        SimpleSymbol simpleSymbol = (SimpleSymbol) new SimpleSymbol(PropertyTypeConstants.PROPERTY_TYPE_TEXT).readResolve();
        Lit19 = simpleSymbol;
        Lit119 = PairWithPosition.make(simpleSymbol, PairWithPosition.make(Lit19, PairWithPosition.make(Lit19, LList.Empty, "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 615311), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 615306), "/tmp/1605642254870_0.4938021793472529-0/youngandroidproject/../src/appinventor/ai_indraja_nutalapati/SmartParking/Screen1.yail", 615300);
        int[] iArr = new int[2];
        iArr[0] = -1;
        Lit90 = IntNum.make(iArr);
        int[] iArr2 = new int[2];
        iArr2[0] = -49611;
        Lit89 = IntNum.make(iArr2);
        int[] iArr3 = new int[2];
        iArr3[0] = -1;
        Lit80 = IntNum.make(iArr3);
        int[] iArr4 = new int[2];
        iArr4[0] = -11495681;
        Lit78 = IntNum.make(iArr4);
        int[] iArr5 = new int[2];
        iArr5[0] = -1;
        Lit61 = IntNum.make(iArr5);
        int[] iArr6 = new int[2];
        iArr6[0] = -11495681;
        Lit54 = IntNum.make(iArr6);
    }

    public Screen1() {
        ModuleInfo.register(this);
        frame frame2 = new frame();
        this.get$Mnsimple$Mnname = new ModuleMethod(frame2, 1, Lit124, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.onCreate = new ModuleMethod(frame2, 2, "onCreate", FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.android$Mnlog$Mnform = new ModuleMethod(frame2, 3, Lit125, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.add$Mnto$Mnform$Mnenvironment = new ModuleMethod(frame2, 4, Lit126, 8194);
        this.lookup$Mnin$Mnform$Mnenvironment = new ModuleMethod(frame2, 5, Lit127, 8193);
        this.is$Mnbound$Mnin$Mnform$Mnenvironment = new ModuleMethod(frame2, 7, Lit128, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.add$Mnto$Mnglobal$Mnvar$Mnenvironment = new ModuleMethod(frame2, 8, Lit129, 8194);
        this.add$Mnto$Mnevents = new ModuleMethod(frame2, 9, Lit130, 8194);
        this.add$Mnto$Mncomponents = new ModuleMethod(frame2, 10, Lit131, 16388);
        this.add$Mnto$Mnglobal$Mnvars = new ModuleMethod(frame2, 11, Lit132, 8194);
        this.add$Mnto$Mnform$Mndo$Mnafter$Mncreation = new ModuleMethod(frame2, 12, Lit133, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.send$Mnerror = new ModuleMethod(frame2, 13, Lit134, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.process$Mnexception = new ModuleMethod(frame2, 14, "process-exception", FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.dispatchEvent = new ModuleMethod(frame2, 15, Lit135, 16388);
        this.dispatchGenericEvent = new ModuleMethod(frame2, 16, Lit136, 16388);
        this.lookup$Mnhandler = new ModuleMethod(frame2, 17, Lit137, 8194);
        ModuleMethod moduleMethod = new ModuleMethod(frame2, 18, null, 0);
        moduleMethod.setProperty("source-location", "/tmp/runtime5970455105385891077.scm:622");
        lambda$Fn1 = moduleMethod;
        this.$define = new ModuleMethod(frame2, 19, "$define", 0);
        lambda$Fn2 = new ModuleMethod(frame2, 20, null, 0);
        lambda$Fn3 = new ModuleMethod(frame2, 21, null, 0);
        lambda$Fn4 = new ModuleMethod(frame2, 22, null, 0);
        lambda$Fn5 = new ModuleMethod(frame2, 23, null, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        lambda$Fn7 = new ModuleMethod(frame2, 24, null, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        lambda$Fn6 = new ModuleMethod(frame2, 25, null, 0);
        lambda$Fn8 = new ModuleMethod(frame2, 26, null, 0);
        lambda$Fn10 = new ModuleMethod(frame2, 27, null, 0);
        lambda$Fn9 = new ModuleMethod(frame2, 28, null, 0);
        lambda$Fn11 = new ModuleMethod(frame2, 29, null, 0);
        lambda$Fn13 = new ModuleMethod(frame2, 30, null, 0);
        lambda$Fn12 = new ModuleMethod(frame2, 31, null, 0);
        lambda$Fn14 = new ModuleMethod(frame2, 32, null, 0);
        lambda$Fn15 = new ModuleMethod(frame2, 33, null, 0);
        lambda$Fn16 = new ModuleMethod(frame2, 34, null, 0);
        lambda$Fn17 = new ModuleMethod(frame2, 35, null, 0);
        lambda$Fn18 = new ModuleMethod(frame2, 36, null, 0);
        lambda$Fn19 = new ModuleMethod(frame2, 37, null, 0);
        lambda$Fn20 = new ModuleMethod(frame2, 38, null, 0);
        lambda$Fn21 = new ModuleMethod(frame2, 39, null, 0);
        lambda$Fn22 = new ModuleMethod(frame2, 40, null, 0);
        lambda$Fn23 = new ModuleMethod(frame2, 41, null, 0);
        lambda$Fn24 = new ModuleMethod(frame2, 42, null, 0);
        lambda$Fn25 = new ModuleMethod(frame2, 43, null, 0);
        lambda$Fn26 = new ModuleMethod(frame2, 44, null, 0);
        lambda$Fn27 = new ModuleMethod(frame2, 45, null, 0);
        lambda$Fn28 = new ModuleMethod(frame2, 46, null, 0);
        this.login$Click = new ModuleMethod(frame2, 47, Lit82, 0);
        lambda$Fn29 = new ModuleMethod(frame2, 48, null, 0);
        lambda$Fn30 = new ModuleMethod(frame2, 49, null, 0);
        lambda$Fn31 = new ModuleMethod(frame2, 50, null, 0);
        lambda$Fn32 = new ModuleMethod(frame2, 51, null, 0);
        this.register$Click = new ModuleMethod(frame2, 52, Lit92, 0);
        lambda$Fn33 = new ModuleMethod(frame2, 53, null, 0);
        lambda$Fn34 = new ModuleMethod(frame2, 54, null, 0);
        this.FirebaseDB1$TagList = new ModuleMethod(frame2, 55, Lit110, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        this.FirebaseDB1$GotValue = new ModuleMethod(frame2, 56, Lit120, 8194);
    }

    public Object lookupInFormEnvironment(Symbol symbol) {
        return lookupInFormEnvironment(symbol, Boolean.FALSE);
    }

    public void run() {
        CallContext instance = CallContext.getInstance();
        Consumer consumer = instance.consumer;
        instance.consumer = VoidConsumer.instance;
        try {
            run(instance);
            th = null;
        } catch (Throwable th) {
            th = th;
        }
        ModuleBody.runCleanup(instance, th, consumer);
    }

    public final void run(CallContext $ctx) {
        String obj;
        Consumer $result = $ctx.consumer;
        runtime.$instance.run();
        this.$Stdebug$Mnform$St = Boolean.FALSE;
        this.form$Mnenvironment = Environment.make(misc.symbol$To$String(Lit0));
        FString stringAppend = strings.stringAppend(misc.symbol$To$String(Lit0), "-global-vars");
        if (stringAppend == null) {
            obj = null;
        } else {
            obj = stringAppend.toString();
        }
        this.global$Mnvar$Mnenvironment = Environment.make(obj);
        Screen1 = null;
        this.form$Mnname$Mnsymbol = Lit0;
        this.events$Mnto$Mnregister = LList.Empty;
        this.components$Mnto$Mncreate = LList.Empty;
        this.global$Mnvars$Mnto$Mncreate = LList.Empty;
        this.form$Mndo$Mnafter$Mncreation = LList.Empty;
        runtime.$instance.run();
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addGlobalVarToCurrentFormEnvironment(Lit3, ""), $result);
        } else {
            addToGlobalVars(Lit3, lambda$Fn2);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addGlobalVarToCurrentFormEnvironment(Lit4, ""), $result);
        } else {
            addToGlobalVars(Lit4, lambda$Fn3);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addGlobalVarToCurrentFormEnvironment(Lit5, ""), $result);
        } else {
            addToGlobalVars(Lit5, lambda$Fn4);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addGlobalVarToCurrentFormEnvironment(Lit6, lambda$Fn5), $result);
        } else {
            addToGlobalVars(Lit6, lambda$Fn6);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addGlobalVarToCurrentFormEnvironment(Lit31, lambda$Fn8), $result);
        } else {
            addToGlobalVars(Lit31, lambda$Fn9);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addGlobalVarToCurrentFormEnvironment(Lit35, lambda$Fn11), $result);
        } else {
            addToGlobalVars(Lit35, lambda$Fn12);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            runtime.setAndCoerceProperty$Ex(Lit0, Lit36, Lit37, Lit38);
            runtime.setAndCoerceProperty$Ex(Lit0, Lit39, "SmartParking", Lit19);
            runtime.setAndCoerceProperty$Ex(Lit0, Lit40, Boolean.TRUE, Lit41);
            runtime.setAndCoerceProperty$Ex(Lit0, Lit42, Boolean.FALSE, Lit41);
            runtime.setAndCoerceProperty$Ex(Lit0, Lit43, "Responsive", Lit19);
            runtime.setAndCoerceProperty$Ex(Lit0, Lit44, "Slots ", Lit19);
            Values.writeValues(runtime.setAndCoerceProperty$Ex(Lit0, Lit45, Boolean.FALSE, Lit41), $result);
        } else {
            addToFormDoAfterCreation(new Promise(lambda$Fn14));
        }
        this.Login_Block = null;
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addComponentWithinRepl(Lit0, Lit46, Lit47, lambda$Fn15), $result);
        } else {
            addToComponents(Lit0, Lit50, Lit47, lambda$Fn16);
        }
        this.Signup = null;
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addComponentWithinRepl(Lit47, Lit51, Lit52, lambda$Fn17), $result);
        } else {
            addToComponents(Lit47, Lit63, Lit52, lambda$Fn18);
        }
        this.username = null;
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addComponentWithinRepl(Lit47, Lit64, Lit65, lambda$Fn19), $result);
        } else {
            addToComponents(Lit47, Lit66, Lit65, lambda$Fn20);
        }
        this.user = null;
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addComponentWithinRepl(Lit47, Lit67, Lit7, lambda$Fn21), $result);
        } else {
            addToComponents(Lit47, Lit70, Lit7, lambda$Fn22);
        }
        this.password = null;
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addComponentWithinRepl(Lit47, Lit71, Lit72, lambda$Fn23), $result);
        } else {
            addToComponents(Lit47, Lit73, Lit72, lambda$Fn24);
        }
        this.pass = null;
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addComponentWithinRepl(Lit47, Lit74, Lit11, lambda$Fn25), $result);
        } else {
            addToComponents(Lit47, Lit75, Lit11, lambda$Fn26);
        }
        this.login = null;
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addComponentWithinRepl(Lit47, Lit76, Lit77, lambda$Fn27), $result);
        } else {
            addToComponents(Lit47, Lit81, Lit77, lambda$Fn28);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            runtime.addToCurrentFormEnvironment(Lit82, this.login$Click);
        } else {
            addToFormEnvironment(Lit82, this.login$Click);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            EventDispatcher.registerEventForDelegation((HandlesEventDispatching) runtime.$Stthis$Mnform$St, "login", "Click");
        } else {
            addToEvents(Lit77, Lit83);
        }
        this.new_User = null;
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addComponentWithinRepl(Lit47, Lit84, Lit85, lambda$Fn29), $result);
        } else {
            addToComponents(Lit47, Lit86, Lit85, lambda$Fn30);
        }
        this.register = null;
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addComponentWithinRepl(Lit47, Lit87, Lit88, lambda$Fn31), $result);
        } else {
            addToComponents(Lit47, Lit91, Lit88, lambda$Fn32);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            runtime.addToCurrentFormEnvironment(Lit92, this.register$Click);
        } else {
            addToFormEnvironment(Lit92, this.register$Click);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            EventDispatcher.registerEventForDelegation((HandlesEventDispatching) runtime.$Stthis$Mnform$St, "register", "Click");
        } else {
            addToEvents(Lit88, Lit83);
        }
        this.FirebaseDB1 = null;
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addComponentWithinRepl(Lit0, Lit93, Lit17, lambda$Fn33), $result);
        } else {
            addToComponents(Lit0, Lit98, Lit17, lambda$Fn34);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            runtime.addToCurrentFormEnvironment(Lit110, this.FirebaseDB1$TagList);
        } else {
            addToFormEnvironment(Lit110, this.FirebaseDB1$TagList);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            EventDispatcher.registerEventForDelegation((HandlesEventDispatching) runtime.$Stthis$Mnform$St, "FirebaseDB1", "TagList");
        } else {
            addToEvents(Lit17, Lit111);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            runtime.addToCurrentFormEnvironment(Lit120, this.FirebaseDB1$GotValue);
        } else {
            addToFormEnvironment(Lit120, this.FirebaseDB1$GotValue);
        }
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            EventDispatcher.registerEventForDelegation((HandlesEventDispatching) runtime.$Stthis$Mnform$St, "FirebaseDB1", "GotValue");
        } else {
            addToEvents(Lit17, Lit121);
        }
        this.Notifier1 = null;
        if (runtime.$Stthis$Mnis$Mnthe$Mnrepl$St != Boolean.FALSE) {
            Values.writeValues(runtime.addComponentWithinRepl(Lit0, Lit122, Lit21, Boolean.FALSE), $result);
        } else {
            addToComponents(Lit0, Lit123, Lit21, Boolean.FALSE);
        }
        runtime.initRuntime();
    }

    static String lambda3() {
        return "";
    }

    static String lambda4() {
        return "";
    }

    static String lambda5() {
        return "";
    }

    static Object lambda6(Object $calledBy) {
        if (runtime.callYailPrimitive(runtime.yail$Mnequal$Qu, LList.list2(runtime.callYailPrimitive(runtime.yail$Mnnot, LList.list1(runtime.callYailPrimitive(runtime.string$Mnempty$Qu, LList.list1(runtime.get$Mnproperty.apply2(Lit7, Lit8)), Lit9, "is text empty?")), Lit10, "not"), runtime.callYailPrimitive(runtime.yail$Mnnot, LList.list1(runtime.callYailPrimitive(runtime.string$Mnempty$Qu, LList.list1(runtime.get$Mnproperty.apply2(Lit11, Lit8)), Lit12, "is text empty?")), Lit13, "not")), Lit14, "=") == Boolean.FALSE) {
            return runtime.callComponentMethod(Lit21, Lit22, LList.list3("Please enter the details", "Details Required", "OK"), Lit23);
        }
        SimpleSymbol simpleSymbol = Lit3;
        if ($calledBy instanceof Package) {
            $calledBy = runtime.signalRuntimeError(strings.stringAppend("The variable ", runtime.getDisplayRepresentation(Lit15), " is not bound in the current context"), "Unbound Variable");
        }
        runtime.addGlobalVarToCurrentFormEnvironment(simpleSymbol, $calledBy);
        runtime.addGlobalVarToCurrentFormEnvironment(Lit4, runtime.callYailPrimitive(runtime.string$Mnto$Mnlower$Mncase, LList.list1(runtime.get$Mnproperty.apply2(Lit7, Lit8)), Lit16, "downcase"));
        runtime.addGlobalVarToCurrentFormEnvironment(Lit5, runtime.get$Mnproperty.apply2(Lit11, Lit8));
        runtime.setAndCoerceProperty$Ex(Lit17, Lit18, "users", Lit19);
        return runtime.callComponentMethod(Lit17, Lit20, LList.Empty, LList.Empty);
    }

    static Procedure lambda7() {
        return lambda$Fn7;
    }

    static Object lambda8(Object $calledBy) {
        if (runtime.callYailPrimitive(runtime.yail$Mnequal$Qu, LList.list2(runtime.callYailPrimitive(runtime.yail$Mnnot, LList.list1(runtime.callYailPrimitive(runtime.string$Mnempty$Qu, LList.list1(runtime.get$Mnproperty.apply2(Lit7, Lit8)), Lit24, "is text empty?")), Lit25, "not"), runtime.callYailPrimitive(runtime.yail$Mnnot, LList.list1(runtime.callYailPrimitive(runtime.string$Mnempty$Qu, LList.list1(runtime.get$Mnproperty.apply2(Lit11, Lit8)), Lit26, "is text empty?")), Lit27, "not")), Lit28, "=") == Boolean.FALSE) {
            return runtime.callComponentMethod(Lit21, Lit22, LList.list3("Please enter the details", "Details Required", "OK"), Lit30);
        }
        SimpleSymbol simpleSymbol = Lit3;
        if ($calledBy instanceof Package) {
            $calledBy = runtime.signalRuntimeError(strings.stringAppend("The variable ", runtime.getDisplayRepresentation(Lit15), " is not bound in the current context"), "Unbound Variable");
        }
        runtime.addGlobalVarToCurrentFormEnvironment(simpleSymbol, $calledBy);
        runtime.addGlobalVarToCurrentFormEnvironment(Lit4, runtime.callYailPrimitive(runtime.string$Mnto$Mnlower$Mncase, LList.list1(runtime.get$Mnproperty.apply2(Lit7, Lit8)), Lit29, "downcase"));
        runtime.addGlobalVarToCurrentFormEnvironment(Lit5, runtime.get$Mnproperty.apply2(Lit11, Lit8));
        runtime.setAndCoerceProperty$Ex(Lit17, Lit18, "users", Lit19);
        return runtime.callComponentMethod(Lit17, Lit20, LList.Empty, LList.Empty);
    }

    static Procedure lambda10() {
        return lambda$Fn10;
    }

    static Object lambda11() {
        runtime.setAndCoerceProperty$Ex(Lit17, Lit18, "users", Lit19);
        return runtime.callComponentMethod(Lit17, Lit32, LList.list2(runtime.lookupGlobalVarInCurrentFormEnvironment(Lit4, runtime.$Stthe$Mnnull$Mnvalue$St), ""), Lit34);
    }

    static Object lambda9() {
        runtime.setAndCoerceProperty$Ex(Lit17, Lit18, "users", Lit19);
        return runtime.callComponentMethod(Lit17, Lit32, LList.list2(runtime.lookupGlobalVarInCurrentFormEnvironment(Lit4, runtime.$Stthe$Mnnull$Mnvalue$St), ""), Lit33);
    }

    static Object lambda12() {
        runtime.setAndCoerceProperty$Ex(Lit17, Lit18, "PL1", Lit19);
        return runtime.callComponentMethod(Lit17, Lit20, LList.Empty, LList.Empty);
    }

    static Procedure lambda13() {
        return lambda$Fn13;
    }

    static Object lambda14() {
        runtime.setAndCoerceProperty$Ex(Lit17, Lit18, "PL1", Lit19);
        return runtime.callComponentMethod(Lit17, Lit20, LList.Empty, LList.Empty);
    }

    static Object lambda15() {
        runtime.setAndCoerceProperty$Ex(Lit0, Lit36, Lit37, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit0, Lit39, "SmartParking", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit0, Lit40, Boolean.TRUE, Lit41);
        runtime.setAndCoerceProperty$Ex(Lit0, Lit42, Boolean.FALSE, Lit41);
        runtime.setAndCoerceProperty$Ex(Lit0, Lit43, "Responsive", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit0, Lit44, "Slots ", Lit19);
        return runtime.setAndCoerceProperty$Ex(Lit0, Lit45, Boolean.FALSE, Lit41);
    }

    static Object lambda16() {
        runtime.setAndCoerceProperty$Ex(Lit47, Lit36, Lit37, Lit38);
        return runtime.setAndCoerceProperty$Ex(Lit47, Lit48, Lit49, Lit38);
    }

    static Object lambda17() {
        runtime.setAndCoerceProperty$Ex(Lit47, Lit36, Lit37, Lit38);
        return runtime.setAndCoerceProperty$Ex(Lit47, Lit48, Lit49, Lit38);
    }

    static Object lambda18() {
        runtime.setAndCoerceProperty$Ex(Lit52, Lit53, Lit54, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit52, Lit55, Boolean.TRUE, Lit41);
        runtime.setAndCoerceProperty$Ex(Lit52, Lit56, Lit57, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit52, Lit8, "SIGN UP", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit52, Lit58, Lit59, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit52, Lit60, Lit61, Lit38);
        return runtime.setAndCoerceProperty$Ex(Lit52, Lit48, Lit62, Lit38);
    }

    static Object lambda19() {
        runtime.setAndCoerceProperty$Ex(Lit52, Lit53, Lit54, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit52, Lit55, Boolean.TRUE, Lit41);
        runtime.setAndCoerceProperty$Ex(Lit52, Lit56, Lit57, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit52, Lit8, "SIGN UP", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit52, Lit58, Lit59, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit52, Lit60, Lit61, Lit38);
        return runtime.setAndCoerceProperty$Ex(Lit52, Lit48, Lit62, Lit38);
    }

    static Object lambda20() {
        runtime.setAndCoerceProperty$Ex(Lit65, Lit8, "Username", Lit19);
        return runtime.setAndCoerceProperty$Ex(Lit65, Lit48, Lit62, Lit38);
    }

    static Object lambda21() {
        runtime.setAndCoerceProperty$Ex(Lit65, Lit8, "Username", Lit19);
        return runtime.setAndCoerceProperty$Ex(Lit65, Lit48, Lit62, Lit38);
    }

    static Object lambda22() {
        runtime.setAndCoerceProperty$Ex(Lit7, Lit68, Lit69, Lit38);
        return runtime.setAndCoerceProperty$Ex(Lit7, Lit48, Lit62, Lit38);
    }

    static Object lambda23() {
        runtime.setAndCoerceProperty$Ex(Lit7, Lit68, Lit69, Lit38);
        return runtime.setAndCoerceProperty$Ex(Lit7, Lit48, Lit62, Lit38);
    }

    static Object lambda24() {
        runtime.setAndCoerceProperty$Ex(Lit72, Lit8, "Password", Lit19);
        return runtime.setAndCoerceProperty$Ex(Lit72, Lit48, Lit62, Lit38);
    }

    static Object lambda25() {
        runtime.setAndCoerceProperty$Ex(Lit72, Lit8, "Password", Lit19);
        return runtime.setAndCoerceProperty$Ex(Lit72, Lit48, Lit62, Lit38);
    }

    static Object lambda26() {
        runtime.setAndCoerceProperty$Ex(Lit11, Lit68, Lit69, Lit38);
        return runtime.setAndCoerceProperty$Ex(Lit11, Lit48, Lit62, Lit38);
    }

    static Object lambda27() {
        runtime.setAndCoerceProperty$Ex(Lit11, Lit68, Lit69, Lit38);
        return runtime.setAndCoerceProperty$Ex(Lit11, Lit48, Lit62, Lit38);
    }

    static Object lambda28() {
        runtime.setAndCoerceProperty$Ex(Lit77, Lit53, Lit78, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit77, Lit56, Lit79, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit77, Lit8, "Login", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit77, Lit60, Lit80, Lit38);
        return runtime.setAndCoerceProperty$Ex(Lit77, Lit48, Lit62, Lit38);
    }

    static Object lambda29() {
        runtime.setAndCoerceProperty$Ex(Lit77, Lit53, Lit78, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit77, Lit56, Lit79, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit77, Lit8, "Login", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit77, Lit60, Lit80, Lit38);
        return runtime.setAndCoerceProperty$Ex(Lit77, Lit48, Lit62, Lit38);
    }

    public Object login$Click() {
        runtime.setThisForm();
        return Scheme.applyToArgs.apply2(runtime.lookupGlobalVarInCurrentFormEnvironment(Lit6, runtime.$Stthe$Mnnull$Mnvalue$St), "login");
    }

    static Object lambda30() {
        runtime.setAndCoerceProperty$Ex(Lit85, Lit8, "New User?", Lit19);
        return runtime.setAndCoerceProperty$Ex(Lit85, Lit48, Lit62, Lit38);
    }

    static Object lambda31() {
        runtime.setAndCoerceProperty$Ex(Lit85, Lit8, "New User?", Lit19);
        return runtime.setAndCoerceProperty$Ex(Lit85, Lit48, Lit62, Lit38);
    }

    static Object lambda32() {
        runtime.setAndCoerceProperty$Ex(Lit88, Lit53, Lit89, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit88, Lit56, Lit79, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit88, Lit8, "Register Here", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit88, Lit60, Lit90, Lit38);
        return runtime.setAndCoerceProperty$Ex(Lit88, Lit48, Lit62, Lit38);
    }

    static Object lambda33() {
        runtime.setAndCoerceProperty$Ex(Lit88, Lit53, Lit89, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit88, Lit56, Lit79, Lit38);
        runtime.setAndCoerceProperty$Ex(Lit88, Lit8, "Register Here", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit88, Lit60, Lit90, Lit38);
        return runtime.setAndCoerceProperty$Ex(Lit88, Lit48, Lit62, Lit38);
    }

    public Object register$Click() {
        runtime.setThisForm();
        return Scheme.applyToArgs.apply2(runtime.lookupGlobalVarInCurrentFormEnvironment(Lit6, runtime.$Stthe$Mnnull$Mnvalue$St), "register");
    }

    static Object lambda34() {
        runtime.setAndCoerceProperty$Ex(Lit17, Lit94, "https://dazzling-fire-7140.firebaseio.com/", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit17, Lit95, "indraja:nutalapati@gmail:com/", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit17, Lit96, "3Y6ipy4mHpAaEKbQ91PLnRssauGRxbXhX1p44t6Z", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit17, Lit97, "https://smartparkingsystembackend.firebaseio.com/", Lit19);
        return runtime.setAndCoerceProperty$Ex(Lit17, Lit18, "SmartParking", Lit19);
    }

    static Object lambda35() {
        runtime.setAndCoerceProperty$Ex(Lit17, Lit94, "https://dazzling-fire-7140.firebaseio.com/", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit17, Lit95, "indraja:nutalapati@gmail:com/", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit17, Lit96, "3Y6ipy4mHpAaEKbQ91PLnRssauGRxbXhX1p44t6Z", Lit19);
        runtime.setAndCoerceProperty$Ex(Lit17, Lit97, "https://smartparkingsystembackend.firebaseio.com/", Lit19);
        return runtime.setAndCoerceProperty$Ex(Lit17, Lit18, "SmartParking", Lit19);
    }

    public Object FirebaseDB1$TagList(Object $value) {
        Object $value2 = runtime.sanitizeComponentData($value);
        runtime.setThisForm();
        ModuleMethod moduleMethod = runtime.yail$Mnlist$Mnmember$Qu;
        Object lookupGlobalVarInCurrentFormEnvironment = runtime.lookupGlobalVarInCurrentFormEnvironment(Lit4, runtime.$Stthe$Mnnull$Mnvalue$St);
        if ($value2 instanceof Package) {
            $value2 = runtime.signalRuntimeError(strings.stringAppend("The variable ", runtime.getDisplayRepresentation(Lit99), " is not bound in the current context"), "Unbound Variable");
        }
        if (runtime.callYailPrimitive(moduleMethod, LList.list2(lookupGlobalVarInCurrentFormEnvironment, $value2), Lit100, "is in list?") != Boolean.FALSE) {
            if (runtime.callYailPrimitive(runtime.yail$Mnequal$Qu, LList.list2(runtime.lookupGlobalVarInCurrentFormEnvironment(Lit3, runtime.$Stthe$Mnnull$Mnvalue$St), "register"), Lit101, "=") != Boolean.FALSE) {
                return runtime.callComponentMethod(Lit21, Lit22, LList.list3("Username Unavailabel", "Already Exists", "OK"), Lit102);
            }
            return runtime.callYailPrimitive(runtime.yail$Mnequal$Qu, LList.list2(runtime.lookupGlobalVarInCurrentFormEnvironment(Lit3, runtime.$Stthe$Mnnull$Mnvalue$St), "login"), Lit103, "=") != Boolean.FALSE ? Scheme.applyToArgs.apply1(runtime.lookupGlobalVarInCurrentFormEnvironment(Lit31, runtime.$Stthe$Mnnull$Mnvalue$St)) : Values.empty;
        } else if (runtime.callYailPrimitive(runtime.yail$Mnequal$Qu, LList.list2(runtime.lookupGlobalVarInCurrentFormEnvironment(Lit3, runtime.$Stthe$Mnnull$Mnvalue$St), "register"), Lit104, "=") == Boolean.FALSE) {
            return runtime.callYailPrimitive(runtime.yail$Mnequal$Qu, LList.list2(runtime.lookupGlobalVarInCurrentFormEnvironment(Lit3, runtime.$Stthe$Mnnull$Mnvalue$St), "login"), Lit109, "=") != Boolean.FALSE ? Scheme.applyToArgs.apply1(runtime.lookupGlobalVarInCurrentFormEnvironment(Lit31, runtime.$Stthe$Mnnull$Mnvalue$St)) : Values.empty;
        } else {
            runtime.setAndCoerceProperty$Ex(Lit17, Lit18, "users", Lit19);
            runtime.callComponentMethod(Lit17, Lit105, LList.list2(runtime.lookupGlobalVarInCurrentFormEnvironment(Lit4, runtime.$Stthe$Mnnull$Mnvalue$St), runtime.lookupGlobalVarInCurrentFormEnvironment(Lit5, runtime.$Stthe$Mnnull$Mnvalue$St)), Lit106);
            return runtime.callComponentMethod(Lit21, Lit22, LList.list3(runtime.callYailPrimitive(strings.string$Mnappend, LList.list2(runtime.lookupGlobalVarInCurrentFormEnvironment(Lit4, runtime.$Stthe$Mnnull$Mnvalue$St), ", you can login now"), Lit107, "join"), "Registered Successfully", "OK"), Lit108);
        }
    }

    public Object FirebaseDB1$GotValue(Object $tag, Object $value) {
        Object $tag2 = runtime.sanitizeComponentData($tag);
        Object $value2 = runtime.sanitizeComponentData($value);
        runtime.setThisForm();
        ModuleMethod moduleMethod = runtime.yail$Mnequal$Qu;
        if ($tag2 instanceof Package) {
            $tag2 = runtime.signalRuntimeError(strings.stringAppend("The variable ", runtime.getDisplayRepresentation(Lit112), " is not bound in the current context"), "Unbound Variable");
        }
        if (runtime.callYailPrimitive(moduleMethod, LList.list2($tag2, runtime.lookupGlobalVarInCurrentFormEnvironment(Lit4, runtime.$Stthe$Mnnull$Mnvalue$St)), Lit113, "=") == Boolean.FALSE) {
            return Values.empty;
        }
        if (runtime.callYailPrimitive(runtime.yail$Mnequal$Qu, LList.list2($value2 instanceof Package ? runtime.signalRuntimeError(strings.stringAppend("The variable ", runtime.getDisplayRepresentation(Lit99), " is not bound in the current context"), "Unbound Variable") : $value2, runtime.lookupGlobalVarInCurrentFormEnvironment(Lit5, runtime.$Stthe$Mnnull$Mnvalue$St)), Lit114, "=") != Boolean.FALSE) {
            runtime.addGlobalVarToCurrentFormEnvironment(Lit3, "active");
            return runtime.callYailPrimitive(runtime.open$Mnanother$Mnscreen, LList.list1("Slots"), Lit115, "open another screen");
        }
        ModuleMethod moduleMethod2 = runtime.yail$Mnequal$Qu;
        if ($value2 instanceof Package) {
            $value2 = runtime.signalRuntimeError(strings.stringAppend("The variable ", runtime.getDisplayRepresentation(Lit99), " is not bound in the current context"), "Unbound Variable");
        }
        return runtime.callYailPrimitive(moduleMethod2, LList.list2($value2, ""), Lit116, "=") != Boolean.FALSE ? runtime.callComponentMethod(Lit21, Lit22, LList.list3(runtime.callYailPrimitive(strings.string$Mnappend, LList.list2("Please register, ", runtime.lookupGlobalVarInCurrentFormEnvironment(Lit4, runtime.$Stthe$Mnnull$Mnvalue$St)), Lit117, "join"), "User not registered", "OK"), Lit118) : runtime.callComponentMethod(Lit21, Lit22, LList.list3("Username and password do not match", "Login Error", "OK"), Lit119);
    }

    public String getSimpleName(Object object) {
        return object.getClass().getSimpleName();
    }

    public void onCreate(Bundle icicle) {
        AppInventorCompatActivity.setClassicModeFromYail(true);
        super.onCreate(icicle);
    }

    public void androidLogForm(Object message) {
    }

    public void addToFormEnvironment(Symbol name, Object object) {
        androidLogForm(Format.formatToString(0, "Adding ~A to env ~A with value ~A", name, this.form$Mnenvironment, object));
        this.form$Mnenvironment.put(name, object);
    }

    public Object lookupInFormEnvironment(Symbol name, Object default$Mnvalue) {
        boolean x = ((this.form$Mnenvironment == null ? 1 : 0) + 1) & true;
        if (x) {
            if (!this.form$Mnenvironment.isBound(name)) {
                return default$Mnvalue;
            }
        } else if (!x) {
            return default$Mnvalue;
        }
        return this.form$Mnenvironment.get(name);
    }

    public boolean isBoundInFormEnvironment(Symbol name) {
        return this.form$Mnenvironment.isBound(name);
    }

    public void addToGlobalVarEnvironment(Symbol name, Object object) {
        androidLogForm(Format.formatToString(0, "Adding ~A to env ~A with value ~A", name, this.global$Mnvar$Mnenvironment, object));
        this.global$Mnvar$Mnenvironment.put(name, object);
    }

    public void addToEvents(Object component$Mnname, Object event$Mnname) {
        this.events$Mnto$Mnregister = lists.cons(lists.cons(component$Mnname, event$Mnname), this.events$Mnto$Mnregister);
    }

    public void addToComponents(Object container$Mnname, Object component$Mntype, Object component$Mnname, Object init$Mnthunk) {
        this.components$Mnto$Mncreate = lists.cons(LList.list4(container$Mnname, component$Mntype, component$Mnname, init$Mnthunk), this.components$Mnto$Mncreate);
    }

    public void addToGlobalVars(Object var, Object val$Mnthunk) {
        this.global$Mnvars$Mnto$Mncreate = lists.cons(LList.list2(var, val$Mnthunk), this.global$Mnvars$Mnto$Mncreate);
    }

    public void addToFormDoAfterCreation(Object thunk) {
        this.form$Mndo$Mnafter$Mncreation = lists.cons(thunk, this.form$Mndo$Mnafter$Mncreation);
    }

    public void sendError(Object error) {
        RetValManager.sendError(error == null ? null : error.toString());
    }

    public void processException(Object ex) {
        Object apply1 = Scheme.applyToArgs.apply1(GetNamedPart.getNamedPart.apply2(ex, Lit1));
        RuntimeErrorAlert.alert(this, apply1 == null ? null : apply1.toString(), ex instanceof YailRuntimeError ? ((YailRuntimeError) ex).getErrorType() : "Runtime Error", "End Application");
    }

    public boolean dispatchEvent(Component componentObject, String registeredComponentName, String eventName, Object[] args) {
        boolean x;
        SimpleSymbol registeredObject = misc.string$To$Symbol(registeredComponentName);
        if (!isBoundInFormEnvironment(registeredObject)) {
            EventDispatcher.unregisterEventForDelegation(this, registeredComponentName, eventName);
            return false;
        } else if (lookupInFormEnvironment(registeredObject) != componentObject) {
            return false;
        } else {
            try {
                Scheme.apply.apply2(lookupHandler(registeredComponentName, eventName), LList.makeList(args, 0));
                return true;
            } catch (PermissionException exception) {
                exception.printStackTrace();
                if (this == componentObject) {
                    x = true;
                } else {
                    x = false;
                }
                if (!x ? x : IsEqual.apply(eventName, "PermissionNeeded")) {
                    processException(exception);
                } else {
                    PermissionDenied(componentObject, eventName, exception.getPermissionNeeded());
                }
                return false;
            } catch (Throwable exception2) {
                androidLogForm(exception2.getMessage());
                exception2.printStackTrace();
                processException(exception2);
                return false;
            }
        }
    }

    public void dispatchGenericEvent(Component componentObject, String eventName, boolean notAlreadyHandled, Object[] args) {
        Boolean bool;
        boolean x = true;
        Object handler = lookupInFormEnvironment(misc.string$To$Symbol(strings.stringAppend("any$", getSimpleName(componentObject), "$", eventName)));
        if (handler != Boolean.FALSE) {
            try {
                Apply apply = Scheme.apply;
                if (notAlreadyHandled) {
                    bool = Boolean.TRUE;
                } else {
                    bool = Boolean.FALSE;
                }
                apply.apply2(handler, lists.cons(componentObject, lists.cons(bool, LList.makeList(args, 0))));
            } catch (PermissionException exception) {
                exception.printStackTrace();
                if (this != componentObject) {
                    x = false;
                }
                if (!x ? x : IsEqual.apply(eventName, "PermissionNeeded")) {
                    processException(exception);
                } else {
                    PermissionDenied(componentObject, eventName, exception.getPermissionNeeded());
                }
            } catch (Throwable exception2) {
                androidLogForm(exception2.getMessage());
                exception2.printStackTrace();
                processException(exception2);
            }
        }
    }

    public Object lookupHandler(Object componentName, Object eventName) {
        String str = null;
        String obj = componentName == null ? null : componentName.toString();
        if (eventName != null) {
            str = eventName.toString();
        }
        return lookupInFormEnvironment(misc.string$To$Symbol(EventDispatcher.makeFullEventName(obj, str)));
    }

    public void $define() {
        Object reverse;
        Object obj;
        Object reverse2;
        Object obj2;
        Object obj3;
        Object var;
        Object component$Mnname;
        Object obj4;
        Language.setDefaults(Scheme.getInstance());
        try {
            run();
        } catch (Exception exception) {
            androidLogForm(exception.getMessage());
            processException(exception);
        }
        Screen1 = this;
        addToFormEnvironment(Lit0, this);
        Object obj5 = this.events$Mnto$Mnregister;
        while (obj5 != LList.Empty) {
            try {
                Pair arg0 = (Pair) obj5;
                Object event$Mninfo = arg0.getCar();
                Object apply1 = lists.car.apply1(event$Mninfo);
                String obj6 = apply1 == null ? null : apply1.toString();
                Object apply12 = lists.cdr.apply1(event$Mninfo);
                EventDispatcher.registerEventForDelegation(this, obj6, apply12 == null ? null : apply12.toString());
                obj5 = arg0.getCdr();
            } catch (ClassCastException e) {
                WrongType wrongType = new WrongType(e, "arg0", -2, obj5);
                throw wrongType;
            }
        }
        try {
            LList components = lists.reverse(this.components$Mnto$Mncreate);
            addToGlobalVars(Lit2, lambda$Fn1);
            reverse = lists.reverse(this.form$Mndo$Mnafter$Mncreation);
            while (reverse != LList.Empty) {
                Pair arg02 = (Pair) reverse;
                misc.force(arg02.getCar());
                reverse = arg02.getCdr();
            }
            obj = components;
            while (obj != LList.Empty) {
                Pair arg03 = (Pair) obj;
                Object component$Mninfo = arg03.getCar();
                component$Mnname = lists.caddr.apply1(component$Mninfo);
                lists.cadddr.apply1(component$Mninfo);
                Object component$Mnobject = Invoke.make.apply2(lists.cadr.apply1(component$Mninfo), lookupInFormEnvironment((Symbol) lists.car.apply1(component$Mninfo)));
                SlotSet.set$Mnfield$Ex.apply3(this, component$Mnname, component$Mnobject);
                addToFormEnvironment((Symbol) component$Mnname, component$Mnobject);
                obj = arg03.getCdr();
            }
            reverse2 = lists.reverse(this.global$Mnvars$Mnto$Mncreate);
            while (reverse2 != LList.Empty) {
                Pair arg04 = (Pair) reverse2;
                Object var$Mnval = arg04.getCar();
                var = lists.car.apply1(var$Mnval);
                addToGlobalVarEnvironment((Symbol) var, Scheme.applyToArgs.apply1(lists.cadr.apply1(var$Mnval)));
                reverse2 = arg04.getCdr();
            }
            Object obj7 = components;
            obj2 = obj7;
            while (obj2 != LList.Empty) {
                Pair arg05 = (Pair) obj2;
                Object component$Mninfo2 = arg05.getCar();
                lists.caddr.apply1(component$Mninfo2);
                Object init$Mnthunk = lists.cadddr.apply1(component$Mninfo2);
                if (init$Mnthunk != Boolean.FALSE) {
                    Scheme.applyToArgs.apply1(init$Mnthunk);
                }
                obj2 = arg05.getCdr();
            }
            obj3 = obj7;
            while (obj3 != LList.Empty) {
                Pair arg06 = (Pair) obj3;
                Object component$Mninfo3 = arg06.getCar();
                Object component$Mnname2 = lists.caddr.apply1(component$Mninfo3);
                lists.cadddr.apply1(component$Mninfo3);
                callInitialize(SlotGet.field.apply2(this, component$Mnname2));
                obj3 = arg06.getCdr();
            }
        } catch (ClassCastException e2) {
            WrongType wrongType2 = new WrongType(e2, "arg0", -2, obj3);
            throw wrongType2;
        } catch (ClassCastException e3) {
            WrongType wrongType3 = new WrongType(e3, "arg0", -2, obj2);
            throw wrongType3;
        } catch (ClassCastException e4) {
            WrongType wrongType4 = new WrongType(e4, "add-to-global-var-environment", 0, var);
            throw wrongType4;
        } catch (ClassCastException e5) {
            WrongType wrongType5 = new WrongType(e5, "arg0", -2, reverse2);
            throw wrongType5;
        } catch (ClassCastException e6) {
            WrongType wrongType6 = new WrongType(e6, "add-to-form-environment", 0, component$Mnname);
            throw wrongType6;
        } catch (ClassCastException e7) {
            WrongType wrongType7 = new WrongType(e7, "lookup-in-form-environment", 0, obj4);
            throw wrongType7;
        } catch (ClassCastException e8) {
            WrongType wrongType8 = new WrongType(e8, "arg0", -2, obj);
            throw wrongType8;
        } catch (ClassCastException e9) {
            WrongType wrongType9 = new WrongType(e9, "arg0", -2, reverse);
            throw wrongType9;
        } catch (YailRuntimeError exception2) {
            processException(exception2);
        }
    }

    public static SimpleSymbol lambda1symbolAppend$V(Object[] argsArray) {
        LList symbols = LList.makeList(argsArray, 0);
        Apply apply = Scheme.apply;
        ModuleMethod moduleMethod = strings.string$Mnappend;
        Object obj = LList.Empty;
        Object obj2 = symbols;
        while (obj2 != LList.Empty) {
            try {
                Pair arg0 = (Pair) obj2;
                Object arg02 = arg0.getCdr();
                Object car = arg0.getCar();
                try {
                    obj = Pair.make(misc.symbol$To$String((Symbol) car), obj);
                    obj2 = arg02;
                } catch (ClassCastException e) {
                    throw new WrongType(e, "symbol->string", 1, car);
                }
            } catch (ClassCastException e2) {
                throw new WrongType(e2, "arg0", -2, obj2);
            }
        }
        Object apply2 = apply.apply2(moduleMethod, LList.reverseInPlace(obj));
        try {
            return misc.string$To$Symbol((CharSequence) apply2);
        } catch (ClassCastException e3) {
            throw new WrongType(e3, "string->symbol", 1, apply2);
        }
    }

    static Object lambda2() {
        return null;
    }
}
