from tools.infer import infer_interface_id, infer_role, infer_roles_for_two


def test_infer_role_keywords():
    assert infer_role("内部请求（源）") == "source"
    assert infer_role("网关响应（目标）") == "target"
    assert infer_role("第三方回调报文") == "target"
    assert infer_role("响应报文") == "target"
    assert infer_role("请求报文") == "source"


def test_infer_two_tables_default_order():
    a, b = infer_roles_for_two("请求报文", "响应报文")
    assert (a, b) == ("source", "target")


def test_interface_id_from_title():
    assert infer_interface_id("查询订单 queryOrder") == "queryOrder"
    assert infer_interface_id("创建订单 createOrder") == "createOrder"
