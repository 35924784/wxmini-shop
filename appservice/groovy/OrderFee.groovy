import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

def LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

LOG.info("groovy Execute");

//物流费为0   暂时不要物流费了
orderfee.setShippingFee(0);

//测试星级及打折
def levelMap = [1:95,2:90,3:85,4:80,5:75]
//对每个商品执行打折计算
int fee = 0
for (item in order.getOrderItemList()) {
    def discount = 100  //默认100不打折
    if(userext.getLevel() != null){
        discount = levelMap[userext.getLevel()]
    }

    def userDiscountMap = userext.getDiscountMap()
    if(userDiscountMap != null && userDiscountMap.get(item.getGoodscode()) != null ){
        discount = userDiscountMap.get(item.getGoodscode())
    }

    fee = fee +  item.getAmount() * item.getPrice() * discount / 100 ;

    LOG.info("fee:" + fee);  
}

//根据打折后的费用  计算可用积分
def userScore = userext.getScore()
orderfee.setScore(0)   //初始设置可用积分为0

// 0-100之间5个积分 100-200之间10个积分 200-300之间15个积分
def scoreMap=[100:5,200:15,300:30,400:40,500:55] 
//转化为百元
int divFee = Math.ceil(fee/10000)*100
LOG.info("divFee:" + divFee);
int maxScore = scoreMap[divFee] != null ? scoreMap[divFee] : 0   //最大可用积分数
if(divFee >= 600){
    maxScore = Math.ceil(divFee*0.12)
}

if(userScore <= maxScore){
    orderfee.setScore(userScore)
} else {
    orderfee.setScore(maxScore)
}

orderfee.setDiscountAmount(orderfee.getOriginPrice() - fee)