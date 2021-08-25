package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
//컨트롤러에서 검증 로직이 차지하는 부분이 매우 커서 별도의 클래스로 역할을 분리
@Component //스프링 빈에 등록
public class ItemValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
        //파라미터로 넘어오는 클래스가 Item 지원되느냐
        //클래스로 넘어오는 타입과 item이 같냐
        //item의 자식 클래스도 통과한다.
    }

    @Override
    public void validate(Object target, Errors errors) {
        Item item = (Item) target; //target을 Item형으로 캐스팅

        //Erros는 BindingResult의 부모클래스, erros에는 rejectValue있다.

        //rejectValue(), reject(): FieldError, ObjectError를 직접 생성하지 않고 깔끔하게 검증 오류를 다룰 수 있다.
        //검증 로직(필드 룰)
        if(!StringUtils.hasText(item.getItemName())){
            errors.rejectValue("itemName", "required");

        }
        if(item.getPrice() == null || item.getPrice() < 1000  || item.getPrice() > 1000000){
            errors.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }
        if(item.getQuantity() == null || item.getQuantity() >= 9999){
            errors.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() !=null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice <10000){
                //특정 필드의 오류가 아닌 global 오류이기 때문
                errors.reject("totalPriceMin" , new Object[]{10000, resultPrice},null);
            }
        }

    }
}
