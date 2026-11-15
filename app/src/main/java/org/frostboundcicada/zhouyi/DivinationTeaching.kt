package org.frostboundcicada.zhouyi

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 卜筮专题按条目展开式教学解析。
 * 默认收起。阅读页应在对应条目的原文/译文之后调用：
 * DivinationTeachingDisclosure(entry.title)
 * 教学内容独立于原文、译文，不得混写。
 */
private data class DivinationLesson(
    val heading: String,
    val explanation: String,
    val method: String,
    val example: String,
    val caution: String,
)

private val divinationLessons = mapOf(
    "卜筮专题总纲：占筮、观象与古代占验" to DivinationLesson("怎样阅读古代卜筮材料",
        "先分清三个层次：原典记载了什么、古注怎样解释、现代研究者如何判断。占筮是古代礼制与决策文化的一部分，不能把后世流行做法直接倒推为周代制度。",
        "依次记录材料出处、时代、工具、操作步骤、所得卦象、占断文字及实际事件。缺少环节时，应明确标出资料边界。",
        "《周礼》制度记载、春秋筮例和后世钱币法属于不同材料类型，应分别讨论，不能拼成一套自古不变的统一程序。",
        "卦占文本是历史文化材料，不是经过现代科学验证的预测工具；不能替代医疗、法律、财务或安全方面的专业判断。"),
    "《系辞上传》大衍筮法：经文原段与译文" to DivinationLesson("从经文理解大衍筮法",
        "《系辞传》“大衍之数五十，其用四十有九”等语句是讨论蓍筮程序的重要依据。经文简约，具体操作细节还需结合历代注疏，不能把某一家的复原说成经文本身。",
        "先辨认数目与术语，再对照朱熹《蓍卦考误》等注释材料；把经文明言与注家推演分栏记录。",
        "“五十”与“用四十九”说明有一策不参与操作；每一步如何持策、挂策和计余，应注明采用的注家解释。",
        "古注对细节可能有差异；遇到异说应并列，不要为流程图抹平差别。"),
    "大衍筮法术语：分二、挂一、揲四、归奇与十八变" to DivinationLesson("分清蓍法中的关键术语",
        "“分二”“挂一”“揲四”“归奇”“扐”等词描述以策数分组、计数和留余的操作。尤其“奇”“扐”的解释须看具体注本。",
        "同时记录原文、注家释义和实际动作；不同版本或注家解释不同，就分别列出。",
        "“揲四”通常涉及以四策为一组计数；左右两组的具体计余方式，应以采用的注释系统为准。",
        "不要把现代简化演示图当作古代唯一操作规范；图解只说明已注明来源的一种解释。"),
    "《系辞上传》观象：仰观天文与俯察地理" to DivinationLesson("观象：从卦画到解释",
        "观象是观察卦画、阴阳爻位、上下卦及其象征关系。它属于解释方法，不等于一个卦象只有唯一结论。",
        "先确认六爻自下而上的排列，再识别上下卦；随后查卦辞、爻辞及《彖传》《象传》，并注明采用的取象方法。",
        "乾为天、坤为地是通行基本取象；进一步引申到人物或情境时，应标明这是传统象义应用。",
        "象义具有历史语境，不应把单一象征机械套用到现实人物或事件。"),
    "《系辞上传》易有圣人之道四：卜筮与占" to DivinationLesson("理解《系辞》所说的四种功能",
        "《系辞传》以“尚辞、尚变、尚象、尚占”概括易学的不同侧面，涉及言辞、变化、卦象和占问，并非四个互斥流派。",
        "结合上下文辨认每一“尚”所指，再观察经传如何通过卦爻辞、阴阳变化与象数说明义理。",
        "读一条爻辞时，可同时观察文字含义、爻位关系、卦象结构和古人占用方式，但应把各层解释分开。",
        "不要只摘取“尚占”二字，就把整部《周易》缩减为预测手册。"),
    "《周礼》筮人、龟人及古代占筮官制" to DivinationLesson("《周礼》中的龟人与筮人",
        "《周礼》把龟卜与蓍筮放在官职、礼制和国家事务的制度框架中叙述。这是制度文献证据，不等于所有时代的实际执行情况。",
        "分别整理官职、职责、使用场景、仪式程序与器具；再与《左传》《国语》筮例比较。",
        "“龟人”与“筮人”职掌不同，不能把龟甲占卜和蓍草筮法混成同一操作。",
        "《周礼》的成书年代、文本层累及制度理想性存在学术讨论，引用时须说明文献性质。"),
    "《周礼·春官·筮人》九筮名称与文本异文" to DivinationLesson("九筮名称与异文怎样处理",
        "九筮名称涉及《周礼》的术语系统。传本字形、断句和历代注释可能不同，不能仅凭现代列表断言含义已定。",
        "先逐字抄录底本，再核对异体字、通假字和注疏；把原文、异文、注家解释分列并记录卷篇位置。",
        "两个版本某字不同，应保留两种写法并附版本信息，不要静默替换。",
        "没有可靠版本依据的解释应标为待考，不以猜测补齐。"),
    "《礼记·曲礼》卜筮礼制：不过三、不相袭" to DivinationLesson("《曲礼》中的卜筮礼节",
        "《礼记·曲礼》所见卜筮内容，应放在礼仪规范与行为秩序中理解；它不只关乎结果，也涉及何时问、如何问及应有态度。",
        "辨认礼节要求、适用身份和具体情境，再比较其他礼书或史传是否有相同规定。",
        "遇到“卜”“筮”并举时，先判断文本是在概述两类占问，还是叙述某一种程序。",
        "礼书规范性文字不能自动证明所有历史时期都严格照此实行。"),
    "《左传·僖公十五年》晋献公筮嫁伯姬：归妹之睽" to DivinationLesson("春秋筮例：晋献公嫁女",
        "《左传》筮例常把卦象、占辞、解释者判断与后续事件连在一起。重点是还原叙事结构，而不是只摘取吉凶结论。",
        "按“起筮背景—所得卦象—占辞—解释者—决策—后续叙事”整理，并核对卦名和爻辞。",
        "故事中的解释与《周易》经文原句要分开；逸诗、繇辞不应误当通行本卦爻辞。",
        "史传叙事具有编纂立场，不能凭故事结果证明占筮具有预测能力。"),
    "《左传》《国语》筮例：占得卦象之后如何解释" to DivinationLesson("比较《左传》《国语》的筮例",
        "两书筮例可用于研究春秋时期卦象解释、政治决策与历史叙事，但案例之间不必然遵循同一规则。",
        "逐例记录人物、所问事项、卦象、引文、解释者和结果；先比较原文结构，不预设统一算法。",
        "同一卦名在不同故事中可能因所引爻辞、变爻或解释方法不同而得到不同说明。",
        "传世筮例是经过记录与编纂的历史材料，不是标准化操作手册。"),
    "钱币占筮与金钱卦：历史层次和文献边界" to DivinationLesson("三钱法的记值与变爻",
        "常见三钱法以三枚钱币一次形成一爻，连续记录六次；常见记值为六、七、八、九，六与九为动爻，七与八为静爻。正反面数值约定可能因流派而异。",
        "开始前写明正反面如何计数；每次记录一个爻值，第一次为初爻，随后逐次向上；六次完成后再区分本卦与之卦。",
        "六为老阴，变为阳；九为老阳，变为阴；七为少阳、八为少阴，保持不变。只交换动爻阴阳。",
        "这是后世常见方法之一，不是《周易》经文规定的唯一筮法；占断规则也有流派差异。"),
    "星象观测与古代星占：与《周易》观象思想的区别" to DivinationLesson("古代星占文献的阅读边界",
        "古代星占把天象与国家、政治或社会事件联系起来，是古代天文知识、政治观念和灾异解释史的一部分。",
        "辨认观测对象、星官名称、时间和出处，再区分天象记录与附加占辞；与现代天文学资料分开对照。",
        "同一星象在不同文献中可能有不同占辞，须保留时代背景和解释者。",
        "古代星占属于历史思想材料，不能当作现代天文学结论或现实事件预测依据。"),
    "星图投钱与五行生克（待考）" to DivinationLesson("待考材料如何保持开放",
        "若材料提到星图、投钱或多枚钱币组合，但缺少可核实的原书、作者、年代和完整步骤，只能作为待考线索保存。",
        "记录最早出处、原始图像、文字上下文、收藏信息和可复核链接；确认图中文字是否原有、是否后配。",
        "来源未明时，只描述现存材料声称了什么，不能自行推演算法并补写成古法。",
        "算法、年代和传承关系均未证实；不得把现代复原伪装成原典。"),
    "龟卜与筮占：材料、程序和历史区别" to DivinationLesson("龟卜与蓍筮的区别",
        "龟卜与蓍筮是古代两种不同占问方式：前者围绕龟甲等材料及兆象，后者围绕蓍策数目与卦爻结构。并称不表示工具和程序相同。",
        "分别记录工具、操作痕迹、结果形式、解释术语和使用场景；考古材料还须核对出土地点、层位和年代。",
        "甲骨卜辞中的兆序、卜辞与周易卦爻辞不是同一套文本系统。",
        "器物用途判断需要考古语境和专业研究支持，不能只凭外形相似定名。"),
    "数字卦与出土筮法：商周至战国材料" to DivinationLesson("怎样研究出土数字卦",
        "出土数字组合常被讨论为数字卦或早期筮数记录。符号含义、阴阳对应及其与传世六爻体系的关系，须依材料逐例论证。",
        "记录出土地点、年代、载体、原始数字与排列方向；比较学者释读方案，并把原始记录和现代释读分开。",
        "把数字转换为阴阳爻之前，必须说明转换规则和证据；不能先套入传世卦象再称其为考古事实。",
        "释读有争议时应并列方案并标注争议，不把推测写成定论。"),
    "卦象、爻象与占断：本卦、变爻和之卦" to DivinationLesson("从卦象、爻象到占断",
        "卦象是上下卦组合形成的整体结构；爻象关注六个位置的阴阳与动静；占断则结合文本、情境和解释传统作判断。三者相关但不可混为一层。",
        "先排本卦，再标动爻并生成之卦；核对卦辞、动爻爻辞及相关《易传》。使用互卦、错卦、综卦或卦变法时，逐项注明规则。",
        "无动爻、单动爻和多动爻在不同占法中可能有不同取辞规则；先声明规则，不能事后随意挑选爻辞。",
        "传统占断规则存在多种体系；解析应说明依据，不将个人发挥冒充经文原义。"),
    "后世易占流派：纳甲、六爻与梅花易数" to DivinationLesson("认识后世易占流派",
        "纳甲、卦气、梅花易数等方法形成于不同历史阶段，符号系统和推演规则不完全相同，不宜统称为周代原始筮法。",
        "记录流派年代、代表文献、作者或传承说法、核心术语、推演规则及现存版本；把可考事实和后世传说分开。",
        "比较流派时，先对照它们对卦、爻、时间和取象的定义，再讨论方法差异。",
        "托名古人的著作或晚出文献须核对版本与成书年代，不能仅凭题署认定作者。"),
    "占筮相关古籍与研究资料索引" to DivinationLesson("如何使用占筮古籍索引",
        "索引帮助定位文献，不代表收录的书都已取得全文、完成校勘或确认图像来源。",
        "尽量记录作者、书名、卷数、版本、藏所、目录信息、数字化入口和本项目实际收录范围；缺项标记待查。",
        "引用某书某卷时，最好同时保存影印页码或叶码，以便复核文字与图版。",
        "目录著录、网页介绍和原书影像是不同证据层级；不能用书目摘要替代原文。")
)

@Composable
fun DivinationTeachingDisclosure(entryTitle: String, modifier: Modifier = Modifier) {
    val lesson = divinationLessons[entryTitle] ?: return
    var expanded by remember(entryTitle) { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp).animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("教学解析｜点击${if (expanded) "收起" else "展开"}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Text(lesson.heading, style = MaterialTheme.typography.bodyMedium)
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "收起教学解析" else "展开教学解析"
                )
            }
            if (expanded) {
                Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LessonParagraph("要点", lesson.explanation)
                    LessonParagraph("阅读方法", lesson.method)
                    LessonParagraph("例子", lesson.example)
                    LessonParagraph("辨析与边界", lesson.caution)
                }
            }
        }
    }
}

@Composable
private fun LessonParagraph(label: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
